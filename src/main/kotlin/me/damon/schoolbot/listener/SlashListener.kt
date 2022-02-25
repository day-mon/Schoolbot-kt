package me.damon.schoolbot.listener

import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SlashListener(private val schoolbot: Schoolbot) : ListenerAdapter()
{
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent)
    {
        if (event.guild == null) return
        //todo preprocessing here
        schoolbot.cmd.handle(event)
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent)
    {
        if (event.guild == null) return
        schoolbot.cmd.handleAutoComplete(event)
    }

}