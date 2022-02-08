package me.damon.schoolbot.commands.main.misc

import dev.minn.jda.ktx.interactions.Option
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.web.*
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.jsoup.Jsoup
import kotlin.math.log

class Search : Command (
    name = "Search",
    description = "Conducts a internet search given with the given parameters",
    category = CommandCategory.MISC,
    options = listOf(
        CommandOptionData<String>(
            type = OptionType.STRING,
            name = "search_results",
            description = "Search query",
            isRequired = true,
        )
    )
 )
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val query = event.getOption("search_results")!!.asString
        val client = event.jda.httpClient
        val request = post("https://html.duckduckgo.com/html/") {
            add("q", query.replace("\\s", "+"))
        }

        client.newCall(request).await(scope = event.scope) { response ->
            when
            {
                response.isSuccessful -> {
                    val body = response.bodyAsString() ?: return@await run {
                        event.replyMessage("There was an issue attempting to get the response body of DuckDuckGo")
                    }

                    val document = Jsoup.parse(body)

                    if (document.getElementsByClass("no-results").hasText())
                    {
                        return@await run {
                            // weird scoping issue just won't let me return??
                            event.replyMessage("No search results for `$query`")
                        }
                    }
                    val url = document.getElementsByClass("result__extras__url")[0].text()
                    val title = document.select("a[href]")[1].text()
                    val snippet = document.getElementsByClass("result__snippet")[0].text()
                    event.replyMessage("https://$url - $title - $snippet")
                }

                response.isRedirect -> {
                    event.replyMessage("DuckDuckGo responded with a redirect status code. Cannot carry out the search")
                }

                response.code() >= 500 -> {
                    logger.error("DuckDuckGo is down?", response.asException())
                    event.replyMessage("DuckDuckGo responded with an internal server error")
                }

                else -> {
                    logger.error("An unexpected error has occurred", response.asException())
                    event.replyMessage("An unexpected error has occurred")
                }

            }
        }
    }
}