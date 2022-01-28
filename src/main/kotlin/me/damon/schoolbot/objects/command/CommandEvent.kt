package me.damon.schoolbot.objects.command

import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(CommandEvent::class.java)

class CommandEvent(
     val schoolbot: Schoolbot,
     val slashEvent: SlashCommandEvent,
     val command: AbstractCommand

)
{
    val jda = slashEvent.jda
    val user = slashEvent.user
    val channel = slashEvent.channel
    val guild = slashEvent.guild
    val member = slashEvent.member
    val hook = slashEvent.hook

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

    fun replyMessage(message: String) = when {
        command.deferredEnabled -> hook.editOriginal(message).queue()
        else -> slashEvent.reply(message).queue()
    }
    fun sendEmbed(embed: MessageEmbed) = channel.sendMessageEmbeds(embed).queue()
    fun sendMessage(message: String) = channel.sendMessage(message).queue()
    fun hasSelfPermissions(permissions: List<Permission>) = command.selfPermission.containsAll(permissions)
    fun hasMemberPermissions(permissions: List<Permission>) = command.memberPermissions.containsAll(permissions)

}