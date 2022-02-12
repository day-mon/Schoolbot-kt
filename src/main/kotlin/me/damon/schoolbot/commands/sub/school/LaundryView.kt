package me.damon.schoolbot.commands.sub.school

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.models.LaundryModel
import me.damon.schoolbot.web.asException
import me.damon.schoolbot.web.await
import me.damon.schoolbot.web.get
import net.dv8tion.jda.api.interactions.commands.OptionType

class LaundryView : SubCommand(
    name = "view",
    category = CommandCategory.SCHOOL,
    description = "Views ",
    options = listOf(
        CommandOptionData<String> (
            type = OptionType.STRING,
            name = "dormitory",
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

        client.newCall(request).await(event.scope) { response ->
            when
            {
                response.isSuccessful ->
                {
                    val json = response.body()?.string() ?: return@await run {
                        event.replyMessage("Error has occurred while trying to get the response body")
                    }

                    logger.debug("Json Response: {}", json)

                    val om = jacksonObjectMapper()
                    val models: List<LaundryModel> = om.readValue(json)
                    event.sendPaginator(models)
                    // add error checking here
                }

                response.code() == 404 ->
                {
                    event.replyMessage("[**$dorm**] does not exist. Please try again!")
                }

                response.code() > 500  ->
                {
                    event.replyMessage("API returned internal server error")
                }

                else ->
                {
                    logger.error("Error has occurred", response.asException() )
                    event.replyMessageWithErrorEmbed("Error occurred in while fetching data from API", response.asException())
                }
            }

        }
    }
}