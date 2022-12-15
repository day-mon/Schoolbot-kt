package me.damon.schoolbot.commands.main.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.ext.bodyAsString
import me.damon.schoolbot.ext.get
import me.damon.schoolbot.handler.ConfigHandler
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.interactions.commands.OptionType
import okhttp3.Request
import org.jsoup.Jsoup
import org.springframework.stereotype.Component


const val WORKER_URL = "https://pitt.damon-worker.workers.dev/"
@Component
class Find(
   configHandler: ConfigHandler
) : Command(
    name = "Find",
    description = "Finds a person within the University of Pittsburgh system",
    deferredEnabled = true,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "name",
            description = "Name of the person you want to find",
            isRequired = true
        )
    ),
    category = CommandCategory.SCHOOL
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val name = event.getOption<String>("name")
        val pittToken = configHandler.config.pittFindToken

        if (pittToken.isBlank()) {
            event.replyErrorEmbed("You must set a PittFind token in order to use this command.")
            return;
        }

        val client = event.jda.httpClient
        val request = Request.Builder()
                .url("$WORKER_URL?name=$name")
                .addHeader("Authorization", "Bearer $pittToken")
                .build()

        client.newCall(request).execute().use { response ->
            if (response.code() == 400) {
                event.replyErrorEmbed("You must set a correct PittFind token in order to use this command.")
                return;
            }


            if (!response.isSuccessful) {
                event.replyErrorEmbed("Something went wrong while trying to find that person.")
                return;
            }


            val body = response.bodyAsString() ?:
                return event.replyErrorEmbed("Something went wrong while trying to find that person.")

            val parsedHtml = Jsoup.parse(body)
            val error = parsedHtml.body().text().contains("Too many people matched your criteria. Please try searching by username, phone, email, or by enclosing your search in quotation marks.")


            if (error) {
               return event.replyErrorEmbed("Too many people matched your criteria. Please try searching by username, phone, email, or by enclosing your search in quotation marks.");
            }

            val persons = parsedHtml.getElementsByClass("row scale-in-center")

            if (persons.isEmpty())
                return event.replyErrorEmbed("No results found for that name.")

            val embeds = persons.map { person ->
                val personsName = person.getElementsByClass("title").text()
                Embed {
                    this.title = personsName ?: "Unknown"
                    person.getElementsByClass("row-label").forEach {
                        field(it.text(), it.nextElementSibling()?.text() ?: "Unknown", false)
                    }
                }
            }

            event.sendPaginator(*embeds.toTypedArray())
        }
    }
}