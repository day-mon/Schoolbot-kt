package me.damon.schoolbot.listener

import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SlashListener(private val schoolbot: Schoolbot) : ListenerAdapter()
{
    override fun onSlashCommand(event: SlashCommandEvent)
    {
        schoolbot.cmd.hande(event)
    }
}