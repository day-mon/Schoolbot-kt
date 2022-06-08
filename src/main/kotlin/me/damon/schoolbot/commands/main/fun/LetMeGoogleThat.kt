package me.damon.schoolbot.commands.main.`fun`

import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class LetMeGoogleThat : Command(
    name = "lmgtfy",
    description = "Let Me Google That For You (lmgtfy) will send a lmgtfy you link with the args you specify",
    category = CommandCategory.FUN,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "search_results",
            description = "Injects search results into LMGTFY link",
            isRequired = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val args = event.getOption<String>("search_results")
            .replace(regex = Constants.SPACE_REGEX, replacement = "+")

        val url = "https://www.letmegooglethat.com/?q=$args"

        event.replyAndEditWithDelay(
            message = "Hold on.... ",
            delayMessage = url,
            duration = 5.seconds
        )
    }
}