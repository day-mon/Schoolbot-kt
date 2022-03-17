package me.damon.schoolbot.ext

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.misc.Emoji
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import yahoofinance.Stock
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

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
            value = "$${quote.price?.parseNumbersWithCommas() ?: "N/A"}"
            inline = false

        }

        field {
            name = "Today's Change"
            value = if (quote.change.toLong() < 0)
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
            value = " $${quote.previousClose.parseNumbersWithCommas()}"
            inline = false
        }

        field {
            name = "Market Cap"
            value = "$${security.stats.marketCap.parseNumbersWithCommas()}"
            inline = false
        }

        field {
            name = "52 Week High"
            value = quote.yearHigh.parseNumbersWithCommas()
        }

        field {
            name = "52 Week Low"
            value = quote.yearLow.parseNumbersWithCommas()
        }

        field {
            name = "52 Week Change"
            value = if (quote.changeFromYearHigh.toLong() < 0)
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

suspend fun ReplyCallbackAction.await(event: CommandEvent): MessageReceivedEvent
= jda.await { it.guild != null && it.author.idLong ==  event.member.idLong && event.channel.idLong == it.channel.idLong }

fun InteractionHook.editOriginalAndClear(content: String) = editMessageById("@original", content)
    .setActionRows(Collections.emptyList())
    .setEmbeds(Collections.emptyList())
    .queue()
