package me.damon.schoolbot.commands.main.info

import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.into
import me.damon.schoolbot.ext.enumSetOf
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.guild.GuildSettings
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.service.GuildService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse

class Settings : Command(
    name = "Settings",
    description = "Allows you to enable/disable guild settings",
    category = CommandCategory.INFO,
    memberPermissions = enumSetOf(Permission.ADMINISTRATOR)
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val settings = event.getService<GuildService>().getGuildSettings(event.guildId)
        event.send(message = "Buttons", embeds = listOf(getCourseSettingsEmbed(event, settings)), actionRows = settingsButtons(event, settings))
    }

    private fun getCourseSettingsEmbed(e: CommandEvent, settings: GuildSettings): MessageEmbed
    {
        val guild = e.guild
        return Embed {
            this.title = "Settings for ${guild.name}"

            field(
                name = "${Emoji.ONE.getAsChat()} Long Message Auto-Upload",
                value = if (settings.longMessageUploading) "Enabled" else "Disabled"
            )
            field(
                name = "${Emoji.TWO.getAsChat()} Delete Course/Assignment after last Reminder",
                if (settings.deleteRemindableEntityOnLastReminder) "Enabled" else "Disabled"
            )
        }
    }

    private fun settingsButtons(event: CommandEvent, settings: GuildSettings): List<ActionRow>
    {
        val jda = event.jda
        val longMessageButton = jda.button(
            style = if (settings.longMessageUploading) ButtonStyle.SUCCESS else ButtonStyle.DANGER,
            emoji = net.dv8tion.jda.api.entities.Emoji.fromUnicode(Emoji.ONE.asUnicode())
        ) {
            settings.apply {
                this.longMessageUploading = this.longMessageUploading.not()
                event.hook.editOriginalEmbeds(getCourseSettingsEmbed(event, settings))
                    .setActionRows(settingsButtons(event, settings)).queue()
            }
            try { event.getService<GuildService>().save(settings) }
            catch (e: Exception) { logger.error("Could not save guild settings for {}", event.guild.name) }
        }


        val deleteEntityButton = jda.button(
            style = if (settings.deleteRemindableEntityOnLastReminder) ButtonStyle.SUCCESS else ButtonStyle.DANGER,
            emoji = net.dv8tion.jda.api.entities.Emoji.fromUnicode(Emoji.TWO.asUnicode())

        ) {
            settings.apply {
                this.deleteRemindableEntityOnLastReminder = this.deleteRemindableEntityOnLastReminder.not()
                event.hook.editOriginalEmbeds(getCourseSettingsEmbed(event, settings))
                    .setActionRows(settingsButtons(event, settings)).queue()
            }
            try { event.getService<GuildService>().save(settings) }
            catch (e: Exception) { logger.error("Could not save guild settings for {}", event.guild.name) }
        }

        val trashButton = jda.button(
            style = ButtonStyle.DANGER,
            emoji =  net.dv8tion.jda.api.entities.Emoji.fromUnicode(Emoji.WASTE_BASKET.asUnicode())
        ) {
            event.hook.deleteOriginal().queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))
        }

        return listOf(longMessageButton, deleteEntityButton, trashButton).into()
    }
}