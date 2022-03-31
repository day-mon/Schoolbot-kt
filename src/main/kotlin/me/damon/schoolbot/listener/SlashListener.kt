package me.damon.schoolbot.listener

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.handler.CommandHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class SlashListener(private val commandHandler: CommandHandler) : ListenerAdapter()
{
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent)
    {
        if (event.guild == null) return
        commandHandler.handle(event)
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent)
    {
        if (event.guild == null) return
        commandHandler.handleAutoComplete(event)
    }

}