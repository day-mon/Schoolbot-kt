package me.damon.schoolbot.ext

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.objects.misc.Emoji
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.Command
import yahoofinance.Stock
import java.math.BigDecimal
import java.time.LocalDateTime

fun Stock.getAsQEmbed(): MessageEmbed
{
    val security = this
    val quote = this.quote
    val securityName = this.name
    return Embed {
        title = "$name Quote (${quote.symbol})"

        field {
            name = "Name"
            value = securityName
        }

        field {
            name = "Outstanding Amount of Security"
            value = BigDecimal.valueOf(security.getStats(true).sharesOutstanding ?: 0).parseNumbersWithCommas()
        }

        field {
            name = "Exchange"
            value = stockExchange
            inline = false
        }

        field {
            name = "Normal Trading Data"
            inline = true
        }

        field {
            name = "Price"
            value = "$${quote.price?.parseNumbersWithCommas() ?: "N/A".removePrefix("$")}"
            inline = false

        }

        field {
            name = "Today's Change"
            value = if (quote.change == null)
            {
                "N/A"
            }
            else if (quote.change.toLong() < 0)
            {
                " ${Emoji.DOWNWARD_TREND.getAsChat()} $${quote.change.parseNumbersWithCommas()}"
            }
            else
            {
                " ${Emoji.UPWARD_TREND.getAsChat()} $${quote.change.parseNumbersWithCommas()}"
            }
            inline = false
        }

        field {
            name = "Average Volume"
            value = BigDecimal.valueOf(quote.avgVolume?: 0).parseNumbersWithCommas()
            inline = false
        }

        field {
            name = "Previous Close"
            value = " $${quote?.previousClose?.parseNumbersWithCommas() ?: "N/A".removePrefix("$")}"
            inline = false
        }

        field {
            name = "Market Cap"
            value = "$${security?.stats?.marketCap?.parseNumbersWithCommas() ?: "N/A".removePrefix("$")}"
            inline = false
        }

        field {
            name = "52 Week High"
            value = quote?.yearHigh?.parseNumbersWithCommas() ?: "N/A"
        }

        field {
            name = "52 Week Low"
            value = quote?.yearLow?.parseNumbersWithCommas() ?: "N/A"
        }

        field {
            name = "52 Week Change"
            value = if (quote.changeFromYearHigh == null)
            {
                "N/A"
            }
            else if (quote.changeFromYearHigh.toLong() < 0)
            {
                " ${Emoji.DOWNWARD_TREND.getAsChat()} ${quote.changeFromYearHigh.parseNumbersWithCommas()}"
            }
            else
            {

                " ${Emoji.UPWARD_TREND.getAsChat()} ${quote.changeFromYearHigh.parseNumbersWithCommas()}"
            }
        }

        footer {
            name = "Live data as of ${LocalDateTime.now().formatDate()}"
        }
    }
}


fun InteractionHook.editOriginalAndClear(content: String) = editMessageById("@original", content)
    .setActionRows(emptyList())
    .setEmbeds(emptyList())
    .queue()


private const val interactionLimit: Int = 25
fun CommandAutoCompleteInteractionEvent.replyChoiceAndLimit(commands: Collection<Command.Choice>) = this.replyChoices(
    commands.take(interactionLimit)
        .filter { it.name.startsWith(this.focusedOption.value, ignoreCase = true) }
)
fun CommandAutoCompleteInteractionEvent.replyChoiceStringAndLimit(commands: Collection<String>) = this.replyChoiceStrings(
    commands.take(interactionLimit)
        .filter { it.startsWith(this.focusedOption.value, ignoreCase = true) }
)
fun CommandAutoCompleteInteractionEvent.replyChoiceStringAndLimit(vararg choices: String) = this.replyChoiceStrings(
    choices.take(interactionLimit)
        .filter { it.startsWith(this.focusedOption.value, ignoreCase = true) }
)
