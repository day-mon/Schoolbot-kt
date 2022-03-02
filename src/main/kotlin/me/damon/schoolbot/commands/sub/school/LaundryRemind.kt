package me.damon.schoolbot.commands.sub.school

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.models.LaundryModel
import me.damon.schoolbot.web.asException
import me.damon.schoolbot.web.bodyAsString
import me.damon.schoolbot.web.get
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.requests.ErrorResponse
import ru.gildor.coroutines.okhttp.await
import java.util.concurrent.TimeUnit

class LaundryRemind : SubCommand(
    name = "remind",
    description = "Reminds a user when their laundry is done",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            type = OptionType.STRING,
            name = "dormitory",
            description = "Target dormitory you want to choose to get reminded from",
            isRequired = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val client = event.jda.httpClient
        val taskHandler = event.schoolbot.taskHandler
        val dorm = event.getOption("dormitory")?.asString
        val request = get("https://johnstown.schoolbot.dev/api/Laundry/${dorm}")

        val response = client.newCall(request).await()
        if (response.isSuccessful.not())
        {
            logger.error("Error has occurred", response.asException() )
            event.replyMessageWithErrorEmbed("Error occurred in while fetching data from API", response.asException())
            return
        }

        val json = response.bodyAsString() ?: return run {
            event.replyMessage("Error has occurred while trying to get the response body")
        }

        val om = jacksonObjectMapper()
        val models: List<LaundryModel> = om.readValue<List<LaundryModel>>(json)
            .filter { it.isInUse && it.timeRemaining.contains("Ext").not() }
            .toList()

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