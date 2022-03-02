package me.damon.schoolbot.commands.sub.school

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.damon.schoolbot.constants
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.asCommandChoice
import me.damon.schoolbot.objects.models.LaundryModel
import me.damon.schoolbot.web.asException
import me.damon.schoolbot.web.bodyAsString
import me.damon.schoolbot.web.get
import net.dv8tion.jda.api.interactions.commands.OptionType
import ru.gildor.coroutines.okhttp.await

class LaundryView : SubCommand(
    name = "view",
    category = CommandCategory.SCHOOL,
    description = "Views laundry in the target dormitory",
    options = listOf(
        CommandOptionData<String>(
            type = OptionType.STRING,
            name = "dormitory",
            choices = constants.dorms.map { it.asCommandChoice() }.toList(),
            description = "Target dormitory you want check",
            isRequired = true
        )
    )

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val client = event.jda.httpClient
        val dorm = event.getOption("dormitory")?.asString
        val request = get("https://johnstown.schoolbot.dev/api/Laundry/${dorm}")


        val response = client.newCall(request).await()

        if (!response.isSuccessful)
        {
            logger.error("An error has while attempting to get the response", response.asException())
            event.replyMessage("An error has occurred while getting the response")
            return
        }

        val json = response.bodyAsString() ?: return run {
            event.replyMessage("Error has occurred while trying to get the response body")
        }

        val om = jacksonObjectMapper()
        val models: List<LaundryModel> = om.readValue(json)
        event.sendPaginator(models)

    }
}