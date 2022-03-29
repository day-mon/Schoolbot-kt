package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.replyPaginator
import dev.minn.jda.ktx.interactions.sendPaginator
import kotlinx.coroutines.CoroutineScope
import me.damon.schoolbot.Constants
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

private val logger = LoggerFactory.getLogger(CommandEvent::class.java)

class CommandEvent(
    val schoolbot: Schoolbot,
    val slashEvent: SlashCommandInteraction,
    val command: AbstractCommand,
    val scope: CoroutineScope,
)
{
    private val executors = Executors.newScheduledThreadPool(3)
    val jda = slashEvent.jda
    val user = slashEvent.user
    val channel = slashEvent.channel
    val guild = slashEvent.guild!!
    val guildId = slashEvent.guild!!.idLong
    val member = slashEvent.member!!
    val hook = slashEvent.hook
    val service = schoolbot.schoolService

    val options: MutableList<OptionMapping> = slashEvent.options

    fun replyEmbed(embed: MessageEmbed, content: String = String.empty) = when {
        command.deferredEnabled -> hook.editOriginalEmbeds(embed).setActionRows(Collections.emptyList()).setContent(content).queue({ }) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
            hook.editOriginal("Error has occurred while attempting to send embeds").queue()
        }
        else -> slashEvent.replyEmbeds(embed).setContent(content).queue({ }) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
            slashEvent.reply("Error has occurred while attempting to send embeds").queue()

        }
    }

    fun replyErrorEmbed(error: String, tit: String = "Error has occurred") = when {
        command.deferredEnabled -> hook.editOriginalEmbeds(
            Embed {
                title = tit
                description = error
                color = Constants.RED
            })
            .setContent(String.empty)
            .setActionRows(Collections.emptyList())
            .queue({ }) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
        }
        else -> slashEvent.replyEmbeds(
            Embed {
            title = tit
            description = error
            color = Constants.RED
        })
            .addActionRows(Collections.emptyList())
            .setContent("")
            .queue({ }) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
        }
    }

    fun replyAndEditWithDelay(message: String, delayMessage: String, unit: TimeUnit, time: Long)
    {
        // expression body looks meh
        if (command.deferredEnabled)
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

    fun replyMessage(message: String) = when {
        command.deferredEnabled -> hook.editOriginal(message).queue()
        else -> slashEvent.reply(message).queue()
    }
    fun replyMessageAndClear(message: String) = when {
        command.deferredEnabled -> hook.editOriginal(message).setActionRows(Collections.emptyList()).setEmbeds(Collections.emptyList()).queue()
        else -> slashEvent.reply(message).addActionRows(Collections.emptyList()).addActionRows(Collections.emptyList()).queue()
    }

    fun replyMessageWithErrorEmbed(message: String, exception: Exception)
    {
        val embed =
            Embed {
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

    fun <T: Pagable> sendPaginator(embeds: Collection<T>) = sendPaginator(*embeds.map { it.getAsEmbed() }.toTypedArray())
    fun <T: Pagable> sendPaginatorColor(embeds: Collection<T>) = sendPaginator(*embeds.map { it.getAsEmbed(guild) }.toTypedArray())

    suspend fun sendMenuAndAwait(menu: SelectMenu, message: String, timeoutDuration: Long = 60, acknowledge: Boolean = false): SelectMenuInteractionEvent
    {
        hook.editOriginal("$message | Time out is set to $timeoutDuration seconds")
            .setActionRow(menu)
            .queue()
        val job = executors.schedule({ hook.run { editOriginal("Command has timed out try again please").setActionRows(Collections.emptyList()).queue() } }, timeoutDuration, TimeUnit.SECONDS)
        return jda.await<SelectMenuInteractionEvent> { it.member!!.idLong == member.idLong && it.componentId == menu.id }.also {
            if (!job.isCancelled)
                job.cancel(true)

            if (acknowledge)
                it.deferEdit().queue()
        }
    }

    suspend fun sendMessageAndAwait(message: String, rows: List<ActionRow> = Collections.emptyList(), timeoutDuration: Long = 60): MessageReceivedEvent
    {
        hook.editOriginal("$message | Time out is set to $timeoutDuration seconds")
            .setActionRows(rows)
            .queue()
        val job = executors.schedule({ hook.run { editOriginal("Command has timed out try again please").setActionRows(Collections.emptyList()).queue() } }, timeoutDuration, TimeUnit.SECONDS)
        return jda.await<MessageReceivedEvent> {it.guild != null && it.member!!.idLong == member.idLong && it.channel.idLong == slashEvent.channel.idLong }.also {
            if (!job.isCancelled)
                job.cancel(true)
        }
    }


    fun sendPaginator(vararg embeds: MessageEmbed)
    {
        if (embeds.isEmpty()) return run { replyErrorEmbed("There are no embeds to display") }
        if (embeds.size == 1) return run { replyEmbed(embeds[0]) }

        if (command.deferredEnabled)
        {


            hook.sendPaginator(
                pages = embeds,
                expireAfter = Duration.parse("5m")
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()

        }
        else
        {
            slashEvent.replyPaginator(
                pages = embeds, expireAfter = Duration.parse("5m")
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()
        }
    }
    fun hasSelfPermissions(permissions: List<Permission>) = guild.selfMember.hasPermission(permissions)
    fun hasMemberPermissions(permissions: List<Permission>) = member.hasPermission(permissions)
    fun sentWithOption(option: String) = slashEvent.getOption(option) != null
    fun getOption(option: String) = slashEvent.getOption(option)
    fun getSentOptions() = command.options.filter { commandOptionData -> commandOptionData.name in slashEvent.options.map { it.name } }
    fun sentWithAnyOptions() = slashEvent.options.isNotEmpty()




}