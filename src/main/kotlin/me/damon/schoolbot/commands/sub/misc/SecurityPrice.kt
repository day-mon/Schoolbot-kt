package me.damon.schoolbot.commands.sub.misc

import dev.minn.jda.ktx.Embed
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
         name = "security_symbol",
         description = "Security you want to get a price of",
         isRequired = true
     )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val security = YahooFinance.get(event.getOption("security_symbol")!!.asString) // blocking
        val quote = security.quote
        event.replyEmbed(
            Embed {
                title = "${security.name} (${security.symbol})"

                field {
                    name = "Price"
                    value = quote.price.toString()
                    inline = true
                }

                field {
                    name = "52 Week High"
                    value = quote.yearHigh.toString()
                    inline = true
                }

                field {
                    name = "52 Week Low"
                    value = quote.yearLow.toString()
                    inline = true
                }
            }
        )
    }
}