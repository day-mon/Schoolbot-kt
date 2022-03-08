package me.damon.schoolbot.commands.sub.misc

import me.damon.schoolbot.ext.getAsQEmbed
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.interactions.commands.OptionType
import yahoofinance.YahooFinance

class SecurityQuote : SubCommand(
    name = "quote",
    category = CommandCategory.MISC,
    description = "Gives a quote of a given security",
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "security_symbol",
            description = "Security you want to get a price of",
            isRequired = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val security = YahooFinance.get(event.getOption("security_symbol")!!.asString) ?: return run {
            event.replyMessage("${event.getOption("security_symbol")!!.asString} does not exist")
        } // blocking

        event.replyEmbed(
            security.getAsQEmbed()
        )

    }
}