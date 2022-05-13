package me.damon.schoolbot.ext

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.misc.Emoji
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction
import yahoofinance.Stock
import java.math.BigDecimal
import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.concurrent.TimeUnit
import kotlin.time.Duration


fun Instant.minus(duration: Duration): Instant = this.minus(duration.inWholeMilliseconds, ChronoUnit.SECONDS)

fun <T> WebhookMessageUpdateAction<T>.queueAfter(duration: Duration) = this.queueAfter(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
fun ZoneId.toOffset(): ZoneOffset = this.rules.getOffset(Instant.now())
fun MessageAction.queueAfter(duration: Duration)  = this.queueAfter(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
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
            value = "$${security.stats?.marketCap?.parseNumbersWithCommas() ?: "N/A".removePrefix("$")}"
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

fun Instant.toDiscordTimeZone() = "<t:${this.epochSecond}>"

fun  IReplyCallback.send(content: String, embed: MessageEmbed? = null, embeds: List<MessageEmbed> = emptyList(), actionRows: List<ActionRow> = emptyList()) =
    if (this.isAcknowledged) this.hook.sendMessage(content).addActionRows(actionRows).addEmbeds(embed?.let { listOf(it) } ?: embeds).queue()
    else this.reply(content).addActionRows(actionRows).addEmbeds(embed?.let { listOf(it) } ?: embeds).queue()

inline fun <reified T> ModalInteractionEvent.getValue(value: String): T? = when (T::class) {
    String::class -> getValue(value)?.asString?.ifEmpty { null } as T?
    Int::class -> getValue(value)?.asString?.toIntOrNull() as T?
    Long::class -> getValue(value)?.asString?.toLongOrNull() as T?
    Double::class -> getValue(value)?.asString?.toDoubleOrNull() as T?
    LocalDate::class -> try { LocalDate.parse(getValue(value)?.asString, Constants.DEFAULT_DATE_FORMAT) } catch (e: Exception) { null } as T?
    LocalTime::class -> try { LocalTime.parse(getValue(value)?.asString, Constants.DEFAULT_TIME_FORMAT) } catch (e: Exception) { null } as T?
    else -> throw IllegalArgumentException("Unknown type ${T::class}")
}

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
