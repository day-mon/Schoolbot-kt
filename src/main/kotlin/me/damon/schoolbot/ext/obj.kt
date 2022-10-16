package me.damon.schoolbot.ext

import dev.minn.jda.ktx.events.await
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.into
import kotlinx.coroutines.withTimeoutOrNull
import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.misc.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import yahoofinance.Stock
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private fun errorEmbed(
    errorString: String
) =  Embed {
        this.title = "${Emoji.STOP_SIGN.getAsChat()} $title"
        this.description = errorString
        this.color = Constants.RED
}

fun Instant.minus(duration: Duration): Instant = this.minus(duration.inWholeMilliseconds, ChronoUnit.MILLIS)


fun MessageEditAction.queueAfter(duration: Duration) = this.queueAfter(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
operator fun LocalDateTime.plus(duration: Duration): LocalDateTime?
= this.plus(duration.inWholeMilliseconds, ChronoUnit.MILLIS)
fun <T> RestAction<T>.queueAfter(duration: Duration) = this.queueAfter(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
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
    .setComponents(emptyList())
    .setEmbeds(emptyList())
    .queue()

fun Instant.toDiscordTimeZone() = "<t:${this.epochSecond}>"
fun Instant.toDiscordTimeZoneRelative() = "<t:${this.epochSecond}:R>"
fun Instant.toDiscordTimeZoneLDST() = "<t:${this.epochSecond}:F>"


fun <T: IReplyCallback> T.replyEmbed(embed: MessageEmbed, content: String = String.empty) = when {
    this.isAcknowledged -> this.hook.sendMessageEmbeds(embed).setContent(content).queue(null) {
        logger.error(
            "Error has occurred while attempting to send embeds",
        )
        hook.editOriginal("Error has occurred while attempting to send embeds").queue()
    }
    else -> this.replyEmbeds(embed).setContent(content).queue(null) {
        logger.error(
            "Error has occurred while attempting to send embeds", it
        )
        this.reply("Error has occurred while attempting to send embeds").queue()

    }
}

fun <T: IReplyCallback> T.replyErrorEmbed(errorString: String, title: String = "Error has occurred", color: Int = Constants.YELLOW): WebhookMessageCreateAction<Message> {

    val embed = Embed {
        this.title = "${Emoji.STOP_SIGN.getAsChat()} $title"
        this.description = errorString
        this.color = color
    }

    if (this.isAcknowledged.not())
        this.deferReply().queue()

    return this.hook.sendMessageEmbeds(embed)
}

fun Message.editErrorEmbed(errorString: String,  title: String = "An error has occurred") = this.edit(components = listOf(), embeds =listOf(Embed {
    this.title = title
    this.description = errorString
    color = Constants.RED
}))

suspend fun <T: CommandInteraction> T.awaitButton(
    message: String = String.empty,
    embed: MessageEmbed? = null,
    embeds: List<MessageEmbed> = listOf(),
    button: Button? = null,
    buttons: List<Button> = listOf(),
    timeout: Duration = 1.minutes
): ButtonInteractionEvent?
{
    val buttonList = button?.let { listOf(it) } ?: buttons
    this.send(
        content = message,
        embeds = embed?.let { listOf(embed) } ?: embeds,
        actionRows = buttonList
    )

    return withTimeoutOrNull(timeout.inWholeMilliseconds) {
        jda.await<ButtonInteractionEvent> { buttonEvent -> buttonEvent.user.idLong == this@awaitButton.member?.idLong && buttonEvent.button.id in buttonList.map { it.id } }
            .also { it.message.editMessageComponents(listOf()).queue() /*remove buttons after*/ }
    }
}

suspend fun <T: IReplyCallback> T.awaitMenu(
    menu: SelectMenu,
    message: String,
    timeoutDuration: Duration = 1.minutes,
    acknowledge: Boolean = false,
    deleteAfter: Boolean = false,
    disableAfter: Boolean = false,
) = withTimeoutOrNull(timeMillis = timeoutDuration.inWholeMilliseconds) {
    if (this@awaitMenu.isAcknowledged) hook.sendMessage("$message | Time out is set to ${timeoutDuration.inWholeMinutes} minute(s)").addActionRow(menu).queue()
    else  this@awaitMenu.reply("$message | Timeout is set to $timeoutDuration minute(s)").addActionRow(menu).queue()

    jda.await<SelectMenuInteractionEvent> { it.member!!.idLong == member?.idLong && it.selectMenu.id == menu.id}
        .also {
            if (acknowledge) it.deferEdit().queue()
            else if (disableAfter) it.editComponents(it.component.asDisabled().into()).queue()
            else if (deleteAfter) it.message.delete().queue(null) { ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE) }
        }
} ?: run {
    replyErrorEmbed("Command has timed out try again please")
    null
}


suspend fun <T: CommandInteraction> T.awaitMenu(
    menu: SelectMenu,
    message: String,
    timeoutDuration: Duration = 1.minutes,
    acknowledge: Boolean = false,
    deleteAfter: Boolean = false,
    disableAfter: Boolean = false,
) = withTimeoutOrNull(timeMillis = timeoutDuration.inWholeMilliseconds) {
    if (this@awaitMenu.isAcknowledged) hook.sendMessage("$message | Time out is set to ${timeoutDuration.inWholeMinutes} minute(s)").addActionRow(menu).queue()
    else this@awaitMenu.reply("$message | Timeout is set to $timeoutDuration minute(s)").addActionRow(menu).queue()


    jda.await<SelectMenuInteractionEvent> { it.member!!.idLong == member?.idLong && it.selectMenu.id == menu.id}
        .also {
            if (acknowledge) it.deferEdit().queue()
            else if (disableAfter) it.editComponents(it.component.asDisabled().into()).queue()
            else if (deleteAfter) it.message.delete().queue(null) { ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE) }
        }
} ?: run {
    replyErrorEmbed("Command has timed out try again please")
    null
}

suspend fun <T: ComponentInteraction> T.awaitMenu(
    menu: SelectMenu,
    message: String,
    timeoutDuration: Duration = 1.minutes,
    acknowledge: Boolean = false,
    deleteAfter: Boolean = false,
    disableAfter: Boolean = false,
) = withTimeoutOrNull(timeoutDuration.inWholeMilliseconds) {
    if (this@awaitMenu.isAcknowledged) hook.sendMessage("$message | Time out is set to ${timeoutDuration.inWholeMinutes} minute(s)").addActionRow(menu).queue()
    else  this@awaitMenu.reply("$message | Timeout is set to $timeoutDuration minute(s)").addActionRow(menu).queue()

    jda.await<SelectMenuInteractionEvent> { it.member!!.idLong == member?.idLong && it.selectMenu.id == menu.id}
        .also {
            if (acknowledge) it.deferEdit().queue()
            else if (disableAfter) it.editComponents(it.component.asDisabled().into()).queue()
            else if (deleteAfter) it.message.delete().queue(null) { ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE) }
        }
} ?: run {
    replyErrorEmbed("Command has timed out try again please")
    null
}

suspend fun <T: Interaction> T.awaitModal(
    modal: Modal,
    duration: Duration = 1.minutes,
    deferReply: Boolean = false,
    deferEdit: Boolean = false
): ModalInteractionEvent?
{

    if (this.isAcknowledged) {
        val errorEmbed = errorEmbed(
            errorString = "This interaction has already been acknowledged. If this keeps occurring please contact that developer."
        )
        this.messageChannel.sendMessageEmbeds(errorEmbed).queue()
        return null
    }

    return withTimeoutOrNull<ModalInteractionEvent>(duration.inWholeMilliseconds) {
        when (this@awaitModal) {
            is CommandInteraction -> this@awaitModal.replyModal(modal).queue()
            is ComponentInteraction -> this@awaitModal.replyModal(modal).queue()
            else -> throw IllegalStateException("${this@awaitModal.javaClass.name} is not a interaction with a replyModal function")
        }
        jda.await { it.member?.idLong == this@awaitModal.member?.idLong && it.modalId == modal.id }
    }.also { if (deferReply) it?.deferReply()?.queue() else if (deferEdit) it?.deferEdit()?.queue()  } ?: run {
        errorEmbed("")
        null
    }
}


suspend fun <T: ComponentInteraction> T.awaitModal(
    modal: Modal,
    duration: Duration = 1.minutes,
    deferReply: Boolean = false,
    deferEdit: Boolean = false,
): ModalInteractionEvent?
{
    if (this.isAcknowledged) {
        this.replyErrorEmbed(
            errorString = "This interaction has already been acknowledged. If this keeps occurring please contact that developer."
        ).queue()
        return null
    }

    return withTimeoutOrNull<ModalInteractionEvent>(duration.inWholeMilliseconds) {
        this@awaitModal.replyModal(modal).queue()
        jda.await { it.member?.idLong == this@awaitModal.member?.idLong && it.modalId == modal.id }
    }.also { if (deferReply) it?.deferReply()?.queue() else if (deferEdit) it?.deferEdit()?.queue()  } ?: run {
        this.replyErrorEmbed(errorString = "This interaction has timed out").queue()
        null
    }
}

fun ModalMapping.asStringTrimmed() = this.asString.trim()

fun IReplyCallback.send(
    content: String,
    embed: MessageEmbed? = null,
    embeds: List<MessageEmbed> = emptyList(),
    ephemeral: Boolean = false,
    actionRows: List<Button> = emptyList()
) = if (this.isAcknowledged) this.hook.sendMessage(content).setEphemeral(ephemeral).addActionRow(actionRows).addEmbeds(embed?.let { listOf(it) } ?: embeds).queue()
    else this.reply(content)
        .setEphemeral(ephemeral)
        .addActionRow(actionRows)
        .addEmbeds(embed?.let { listOf(it) } ?: embeds)
        .queue()

inline fun <reified T> ModalInteractionEvent.getValue(id: String): T? {
    val value = getValue(id)?.asStringTrimmed() ?: return null
    if (value.isBlank()) return null

    return when (T::class) {
        String::class -> value.ifEmpty { null } as T?
        Int::class -> value.toIntOrNull() as T?
        Long::class -> value.toLongOrNull() as T?
        Double::class -> value.toDoubleOrNull() as T?
        URI::class -> try { URI.create(value) } catch (e: Exception) { null }  as T?
        Role::class ->  this.guild?.getRoleById(value) as T?
        TextChannel::class -> this.guild?.getTextChannelById(value) as T?
        LocalDate::class -> try { LocalDate.parse(value, Constants.DEFAULT_DATE_FORMAT) } catch (e: Exception) { null } as T?
        LocalTime::class -> try { LocalTime.parse(value, Constants.DEFAULT_TIME_FORMAT) } catch (e: Exception) { null } as T?
        else -> throw IllegalArgumentException("Unknown type ${T::class}")
    }
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
