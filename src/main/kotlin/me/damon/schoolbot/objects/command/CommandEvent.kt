package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.events.await
import dev.minn.jda.ktx.interactions.components.replyPaginator
import dev.minn.jda.ktx.interactions.components.sendPaginator
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import me.damon.schoolbot.Constants
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.ext.replyErrorEmbed
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.misc.Pagable
import me.damon.schoolbot.service.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class CommandEvent(
    val schoolbot: Schoolbot,
    val slashEvent: SlashCommandInteraction,
    val command: AbstractCommand,
    val scope: CoroutineScope,
)
{
    val logger by SLF4J
    val jda = slashEvent.jda
    val user = slashEvent.user
    val channel = slashEvent.channel
    val guild = slashEvent.guild!!
    val guildId = slashEvent.guild!!.idLong
    val member = slashEvent.member!!
    val hook = slashEvent.hook
    val service = schoolbot.schoolService

    val options: MutableList<OptionMapping> = slashEvent.options



    fun replyEmbed(embed: MessageEmbed, content: String = String.empty) = replyEmbed(slashEvent, embed, content)
    fun replyErrorEmbed(error: String, embedTitle: String = "Error has occurred") = replyErrorEmbed(slashEvent, "${Emoji.RED_CIRCLE.getAsChat()} $error", embedTitle)



    fun <T: IReplyCallback> replyEmbed(interaction: T, embed: MessageEmbed, content: String = String.empty) = when {
        command.deferredEnabled || slashEvent.isAcknowledged -> interaction.hook.sendMessageEmbeds(embed).queue(null) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
            hook.editOriginal("Error has occurred while attempting to send embeds").queue()
        }
        else -> interaction.replyEmbeds(embed).setContent(content).queue(null) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
            interaction.reply("Error has occurred while attempting to send embeds").queue()

        }
    }
    fun <T: IReplyCallback> replyErrorEmbed(interaction: T, error: String, tit: String = "Error has occurred") = when
    {
        command.deferredEnabled || interaction.isAcknowledged -> interaction.hook.sendMessageEmbeds(Embed {
            title = tit
            description = error
            color = Constants.RED
        }).queue(null) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}",
                it
            )
        }
        else -> interaction.replyEmbeds(Embed {
            title = tit
            description = error
            color = Constants.RED
        }).queue(null) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}",
                it
            )
        }
    }



    fun replyAndEditWithDelay(message: String, delayMessage: String, unit: TimeUnit, time: Long)
    {
        // expression body looks meh
        if (command.deferredEnabled || slashEvent.isAcknowledged)
        {
            hook.editOriginal(message).queue {
                it.editMessage(delayMessage).queueAfter(time, unit)
            }
        }
        else
        {
            slashEvent.reply(message).queue {
                it.editOriginal(delayMessage).queueAfter(time, unit)
            }
        }
    }

    fun replyMessage(message: String) = when  {
        command.deferredEnabled || slashEvent.isAcknowledged -> hook.sendMessage(message).queue()
        else -> slashEvent.reply(message).queue()
    }

    fun replyMessageAndClear(message: String) = when  {
        command.deferredEnabled || slashEvent.isAcknowledged -> hook.editOriginal(message).setActionRows(emptyList()).setEmbeds(emptyList()).queue()
        else -> slashEvent.reply(message).addActionRows(emptyList()).addActionRows(emptyList()).queue()
    }

    fun replyMessageWithErrorEmbed(message: String, exception: Exception)
    {
        val embed = Embed {
            title = "Error occurred. Send this message to a developer if it constantly occurs"
            field {
                title = "Cause"
                value = exception.cause.toString()
                inline = true
            }
            description = """```kt
                            ${exception.stackTraceToString()}
                        ```""".trimIndent()
        }
        if (command.deferredEnabled)
        {
            hook.editOriginal(message)
                .setEmbeds(embed).queue()

        }
        else
        {
            slashEvent.reply(message)
                .addEmbeds(embed).queue()
        }
    }



    fun <T : Pagable> sendPaginator(embeds: Collection<T>) =
        sendPaginator(*embeds.map { it.getAsEmbed() }.toTypedArray())

    fun <T : Pagable> sendPaginatorColor(embeds: Collection<T>) =
        sendPaginator(*embeds.map { it.getAsEmbed(guild) }.toTypedArray())


    suspend fun <T: IModalCallback> awaitModal(interaction: T, modal: Modal, duration: Duration = 1.minutes, deferReply: Boolean = false): ModalInteractionEvent?
    {
        if (interaction.isAcknowledged)
        {
            replyErrorEmbed("Interaction is already acknowledged")
            return null
        }

        return withTimeoutOrNull<ModalInteractionEvent>(duration.inWholeMilliseconds) {
            interaction.replyModal(modal).queue()
            jda.await { it.member?.idLong == slashEvent.member?.idLong && it.modalId == modal.id }
        }.also { if (deferReply) it?.deferReply()?.queue() } ?: run {
            replyErrorEmbed("Command timed out")
            null
        }
    }



    /**
     * This function is used to send a paginator of embeds.
     * @param embeds The embeds to send
     * @param timeoutDuration The duration in seconds before the paginator times out
     * @param acknowledge whether to acknowledge the message
     * @return The interaction event
     *
     */
    suspend fun awaitMenu(
        menu: SelectMenu, message: String, timeoutDuration: Long = 1, acknowledge: Boolean = false, throwAway: Boolean = false
    ) = withTimeoutOrNull(timeoutDuration * 60000) {
        if (slashEvent.isAcknowledged || command.deferredEnabled) hook.sendMessage("$message | Time out is set to $timeoutDuration minute(s)").addActionRow(menu).queue()
        else  slashEvent.reply("$message | Timeout is set to $timeoutDuration minute(s)").addActionRow(menu).queue()

        jda.await<SelectMenuInteractionEvent> { it.member!!.idLong == member.idLong && it.channel.idLong == slashEvent.channel.idLong }
            .also { if (acknowledge) it.deferEdit().queue(); if (throwAway) it.message.delete().queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)) }
    } ?: run {
        replyErrorEmbed("Command has timed out try again please")
        null
    }


    /**
     * This function is used to send a paginator of embeds.
     * @param embeds The embeds to send
     * @param timeoutDuration The duration in seconds before the paginator times out
     * @param acknowledge whether to acknowledge the message
     * @return The interaction event
     *
     */
    suspend fun sendMessageAndAwait(
        message: String,
        rows: List<ActionRow> = emptyList(),
        timeoutDuration: Long = 1
    ): MessageReceivedEvent = withTimeoutOrNull(timeoutDuration * 60000) {
        hook.editOriginal(message).setActionRows(rows).queue()
        jda.await { it.member!!.idLong == member.idLong && it.channel.idLong == slashEvent.channel.idLong }
    } ?: run {
        hook.replyErrorEmbed(body = "Command has timed out try again please")
        throw TimeoutException("Command has timed out")
    }


    fun sendPaginator(vararg embeds: MessageEmbed)
    {
        if (embeds.isEmpty()) return  replyErrorEmbed("There are no embeds to display")
        if (embeds.size == 1) return  replyEmbed(embeds.first())

        if (command.deferredEnabled || slashEvent.isAcknowledged)
        {


            hook.sendPaginator(
                pages = embeds, expireAfter = 5.minutes
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()

        }
        else
        {
            slashEvent.replyPaginator(
                pages = embeds, expireAfter = 5.minutes
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()
        }
    }
    fun hasSelfPermissions(permissions: List<Permission>) = guild.selfMember.hasPermission(permissions)
    fun hasMemberPermissions(permissions: List<Permission>) = member.hasPermission(permissions)
    fun sentWithOption(option: String) = slashEvent.getOption(option) != null
    inline fun <reified T> getOption(name: String): T = when (T::class)
    {
        String::class -> slashEvent.getOption(name)?.asString as T
        // Could break if number is over 2.147 billion lol
        Int::class -> slashEvent.getOption(name)?.asLong?.toInt() as T
        Long::class -> slashEvent.getOption(name)?.asLong as T
        Double::class -> slashEvent.getOption(name)?.asDouble as T
        Boolean::class -> slashEvent.getOption(name)?.asBoolean as T
        Member::class -> slashEvent.getOption(name)?.asMember as T
        else -> throw IllegalArgumentException("Unknown type ${T::class}")
    }

    inline fun <reified T: SpringService> getService(): T = when (T::class)
    {
        is GuildService -> schoolbot.guildService as T
        is SchoolService -> schoolbot.schoolService as T
        is ProfessorService -> schoolbot.professorService as T
        is CourseService -> schoolbot.courseService as T
        is AssignmentService -> schoolbot.assignmentService as T
        is AssignmentReminderService -> schoolbot.assignmentReminderService as T
        else -> throw IllegalArgumentException("Unknown type ${T::class}")
    }

    fun getOption(option: String) = slashEvent.getOption(option)
    fun getSentOptions() =
        command.options.filter { commandOptionData -> commandOptionData.name in slashEvent.options.map { it.name } }

    fun sentWithAnyOptions() = slashEvent.options.isNotEmpty()


}