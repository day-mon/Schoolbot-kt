package me.damon.schoolbot.commands.sub.school

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import me.damon.schoolbot.constants
import me.damon.schoolbot.ext.asCommandChoice
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.concurrent.TimeUnit

class LaundryReminderAdd : SubCommand(
    name = "add",
    description = "Reminds a user when their laundry is done",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "dormitory",
            choices = constants.dorms.map { it.asCommandChoice() }.toList(),
            description = "Target dormitory you want to choose to get reminded from",
            isRequired = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val taskHandler = event.schoolbot.taskHandler
        val dorm = event.getOption("dormitory")?.asString
        val response = event.schoolbot.apiHandler.johnstownAPI.getLaundryItems(dorm!!)


        if (response.isSuccessful.not())
        {
            logger.error("Error has occurred", response.raw().asException() )
            event.replyMessageWithErrorEmbed("Error occurred in while fetching data from API", response.raw().asException())
            return
        }

        val models = response.body()?.filter { it.isInUse && it.timeRemaining.contains("Ext").not() || it.timeRemaining.contains("Offline").not() }?.toList()
            ?: return run {
            event.replyMessage("Error has occurred while trying to get the response body")
        }

        if (models.isEmpty()) return run { event.replyMessage("There is no machine that is in use for you to be reminded about")}

        val menu = SelectMenu("laundry:menu")
        { models.forEachIndexed { index, model -> option("${model.type} - ${model.timeRemaining}", index.toString()) } }

        val selectionEvent = event.sendMenuAndAwait(
            menu = menu,
            message = "Please select the machine you would like to be reminded of"
        )
        val option = models[selectionEvent.values[0].toInt()]
        val timeLeft = option.timeRemaining.split(Regex("\\s+"))[0].toInt()
        val id = "${event.user.idLong}_${option.location}_${option.type}_${option.applianceID}"

        if (taskHandler.taskExist(id)) return run {
            event.replyMessage("You already have a reminder for this ${option.location} ${option.applianceID}")
        }

        taskHandler.addTask(
            name = id,
            timeUnit = TimeUnit.MINUTES,
            duration = timeLeft.toLong(),
            block =  {
                val message = "${option.type} - ${option.applianceID} at ${option.location} is now ready"
                event.user.openPrivateChannel()
                    .queue { pc ->
                        pc.sendMessage(message).queue({}, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                            event.replyMessage("I could not send you a message but.. $message")
                        })
                    }
            }
        )

        selectionEvent.reply("You will be reminded in $timeLeft minutes about your laundry").queue()

    }
}