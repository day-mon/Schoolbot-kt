package me.damon.schoolbot.commands.main.info

import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class Settings : Command(
    name = "Settings",
    description = "Allows you to enable/disable guild settings",
    category = CommandCategory.INFO
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val settings = event.schoolbot.guildService.getGuildSettings(event.guildId)
        val embed = Embed {
            title = "Guild Settings"

            field {
                name = "Long Message Auto-Upload"
                value = if (settings.longMessageUploading) "Enabled" else "Disabled"
            }
        }

        val buttonUpload = event.jda.button(
            style = if (settings.longMessageUploading) ButtonStyle.DANGER else ButtonStyle.SUCCESS,
            listener = {
                settings.longMessageUploading = !settings.longMessageUploading
            }

        )
    }
}