package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.replyPaginator
import dev.minn.jda.ktx.interactions.sendPaginator
import kotlinx.coroutines.CoroutineScope
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.constants
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

private val logger = LoggerFactory.getLogger(CommandEvent::class.java)

class CommandEvent(
    val schoolbot: Schoolbot,
    val slashEvent: SlashCommandInteractionEvent,
    val command: AbstractCommand,
    val scope: CoroutineScope,
)
{

    private val executors = Executors.newScheduledThreadPool(3)
    val jda = slashEvent.jda
    val user = slashEvent.user
    val channel = slashEvent.channel
    val guild = slashEvent.guild!!
    val member = slashEvent.member!!
    val hook = slashEvent.hook
    val options: MutableList<OptionMapping> = slashEvent.options

    fun replyEmbed(embed: MessageEmbed) = when {
        command.deferredEnabled -> hook.editOriginalEmbeds(embed).queue({ }) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
        }
        else -> slashEvent.replyEmbeds(embed).queue({ }) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
        }
    }

    fun replyErrorEmbed(error: String, tit: String = "Error has occurred") = when {
        command.deferredEnabled -> hook.editOriginalEmbeds(
            Embed {
                title = tit
                description = error
                color = constants.red
            })
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
            color = constants.red
        })
            .addActionRows(Collections.emptyList())
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

    fun <T: Pagable> sendPaginator(embeds: List<T>) = sendPaginator(*embeds.map { it.getAsEmbed() }.toTypedArray())



    suspend fun sendMenuAndAwait(menu: SelectMenu, message: String, timeoutDuration: Long = 60): SelectMenuInteractionEvent
    {
        hook.editOriginal(message)
            .setActionRow(menu)
            .queue()
        val job = executors.schedule({ hook.run { editOriginal("Command has timed out try again please").setActionRows(Collections.emptyList()).queue() } }, timeoutDuration, TimeUnit.SECONDS)
        return jda.await<SelectMenuInteractionEvent> { it.member!!.idLong == member.idLong && it.componentId == menu.id }.also {
            if (!job.isCancelled)
                job.cancel(true)
        }
    }

    fun sendPaginator(vararg embeds: MessageEmbed)
    {

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





}