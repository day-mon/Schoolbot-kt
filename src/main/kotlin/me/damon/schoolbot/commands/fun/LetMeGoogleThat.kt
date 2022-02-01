package me.damon.schoolbot.commands.`fun`

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.TimeUnit

private val SPACES = Regex("\\s+")

class LetMeGoogleThat : Command(
    name = "lmgtfy",
    description = "Let Me Google That For You (lmgtfy) will send a lmgtfy you link with the args you specify",
    category = CommandCategory.FUN,
    options = listOf(
        CommandOptionData<String>(OptionType.STRING, "search_results", "Injects search results into LMGTFY link", true)
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val args = event.getOption("search_results")!!
            .asString
            .replace(regex = SPACES, replacement = "+")

        val url = "https://www.letmegooglethat.com/?q=$args"

        event.replyAndEditWithDelay("Hold on.... ", url, TimeUnit.SECONDS, 5)
    }
}