package me.damon.schoolbot.commands.main.misc

import me.damon.schoolbot.ext.await
import me.damon.schoolbot.ext.bodyAsString
import me.damon.schoolbot.ext.post
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.jsoup.Jsoup

class Search : Command (
    name = "Search",
    description = "Conducts a internet search given with the given parameters",
    category = CommandCategory.MISC,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "search_results",
            description = "Search query",
            isRequired = true,
        )
    )
 )
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val query = event.getOption<String>("search_results")
        val client = event.jda.httpClient
        val request = post("https://html.duckduckgo.com/html/") {
            add("q", query.replace("\\s", "+"))
        }


        val response = client.newCall(request).await()

        if (!response.isSuccessful) return event.replyMessage("There was an issue attempting to get the response body of DuckDuckGo")


        val body = response.bodyAsString() ?: return  event.replyMessage("There was an issue attempting to get the response body of DuckDuckGo")
        val document = Jsoup.parse(body)

        if (document.getElementsByClass("no-results").hasText()) return event.replyMessage("No search results for `$query`")

        val url =  document.getElementsByClass("result__extras__url")
        if (url.isNullOrEmpty()) return event.replyMessage("Could not target url on `$query`")
        val urlText = url.first()!!.text()

        val title = document.select("a[href]")
        if (title.isNullOrEmpty() || title.size == 1) return event.replyMessage("Could not target the title on`$query`")
        val titleText = title[1].text()

        val snippet = document.getElementsByClass("result__snippet")
        if (snippet.isNullOrEmpty()) return event.replyMessage("Could not target snippet for `$query`")
        val snippetText = snippet.first()!!.text()

        event.replyMessage("https://$urlText - $titleText - $snippetText")

    }
}