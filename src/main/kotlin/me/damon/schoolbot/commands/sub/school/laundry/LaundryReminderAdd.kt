package me.damon.schoolbot.commands.sub.school.laundry

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.asCommandChoice
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.handler.ApiHandler
import me.damon.schoolbot.handler.TaskHandler
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.requests.ErrorResponse
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LaundryReminderAdd(
    private val taskHandler: TaskHandler,
    private val apiHandler: ApiHandler
) : SubCommand(
    name = "add",
    description = "Reminds a user when their laundry is done",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "dormitory",
            choices = Constants.DORMS.map { it.asCommandChoice() }.toList(),
            description = "Target dormitory you want to choose to get reminded from",
            isRequired = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val dorm = event.getOption<String>("dormitory")
        val response = apiHandler.johnstownAPI.getLaundryItems(dorm)

        if (response.isSuccessful.not())
        {
            logger.error("Error has occurred", response.raw().asException() )
            return event.replyErrorEmbed("Error occurred in while fetching data from API")

        }

        val models = response.body()?.filter { it.isInUse && !(it.timeRemaining.contains("Ext") || it.timeRemaining.contains("Offline")) }?.toList()
            ?: run {
                logger.debug("{}", response.errorBody())
                return event.replyMessage("Error has occurred while trying to get the response body")
            }

        if (models.isEmpty()) return run { event.replyMessage("There is no machine that is in use for you to be reminded about")}

        val menu = SelectMenu("laundry:menu")
        { models.forEachIndexed { index, model -> option("${model.type} - ${model.timeRemaining}", index.toString()) } }

        val selectionEvent = event.awaitMenu(
            menu = menu,
            message = "Please select the machine you would like to be reminded of"
        ) ?: return
        val option = models[selectionEvent.values.first().toInt()]
        val timeLeft = option.timeRemaining.split(Constants.SPACE_REGEX).first().toInt()
        val id = "${event.user.idLong}_${option.location}_${option.type}_${option.applianceID}"

        if (taskHandler.taskExist(id)) return event.replyMessage("You already have a reminder for this ${option.location} ${option.applianceID}")


        taskHandler.addTask(
            name = id,
            timeUnit = TimeUnit.MINUTES,
            duration = timeLeft.toLong(),
            block =  {
                val message = "The ${if (option.type.lowercase().contains("d")) "Dryer" else "Washer"} at ${option.location} is now finished."
                event.user.openPrivateChannel()
                    .queue { pc ->
                        pc.sendMessage(message).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                            event.replyMessage("I could not send you a message but.. $message")
                        })
                    }
            }
        )

        selectionEvent.reply("You will be reminded in $timeLeft minutes about your laundry").queue()

    }
}