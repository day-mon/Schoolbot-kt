package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.replyPaginator
import dev.minn.jda.ktx.interactions.sendPaginator
import kotlinx.coroutines.CoroutineScope
import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

private val logger = LoggerFactory.getLogger(CommandEvent::class.java)

class CommandEvent(
    val schoolbot: Schoolbot,
    val slashEvent: SlashCommandEvent,
    val command: AbstractCommand,
    val scope: CoroutineScope
)
{
    val jda = slashEvent.jda
    val user = slashEvent.user
    val channel = slashEvent.channel
    val guild = slashEvent.guild
    val member = slashEvent.member
    val hook = slashEvent.hook
    val options = slashEvent.options

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


    fun replyMessageWithErrorEmbed(message: String, exception: Exception)
    {
        if (command.deferredEnabled)
        {
            hook.editOriginal(message)
                .setEmbeds(
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
                    }).queue()

        }
        else
        {
            slashEvent.reply(message)
                .addEmbeds(
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
                    }).queue()
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
    fun hasSelfPermissions(permissions: List<Permission>) = command.selfPermission.containsAll(permissions)
    fun hasMemberPermissions(permissions: List<Permission>) = command.memberPermissions.containsAll(permissions)
    fun sentWithOption(option: String) = slashEvent.getOption(option) != null
    fun getOption(option: String) = slashEvent.getOption(option)
    fun sentWithAnyOptions() = slashEvent.options.isNotEmpty()
    fun getSentOptions() = command.options.filter { commandOptionData -> commandOptionData.name in slashEvent.options.map { it.name } }





}