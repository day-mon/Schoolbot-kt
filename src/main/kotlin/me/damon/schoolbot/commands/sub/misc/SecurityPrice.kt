package me.damon.schoolbot.commands.sub.misc

import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.interactions.commands.OptionType
import yahoofinance.YahooFinance

class SecurityPrice : SubCommand(
    name = "price",
    category = CommandCategory.MISC,
    description = "Gives a price of a given security",
    options = listOf(
     CommandOptionData<String>(
         type = OptionType.STRING,
         name = "security",
         description = "Security you want to get a price of",
         isRequired = true
     )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {

    }
}