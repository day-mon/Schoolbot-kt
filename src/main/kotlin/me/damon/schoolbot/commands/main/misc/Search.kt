package me.damon.schoolbot.commands.main.misc

import dev.minn.jda.ktx.interactions.Option
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.web.await
import me.damon.schoolbot.web.bodyAsString
import me.damon.schoolbot.web.get
import me.damon.schoolbot.web.post
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
                    logger.info("{}", response.bodyAsString())
                }

            }
        }
    }
}