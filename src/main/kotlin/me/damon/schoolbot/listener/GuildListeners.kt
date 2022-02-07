package me.damon.schoolbot.listener

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildListeners : ListenerAdapter()
{
    override fun onGuildJoin(event: GuildJoinEvent)
    {
        super.onGuildJoin(event)
    }

    override fun onGuildLeave(event: GuildLeaveEvent)
    {
        super.onGuildLeave(event)
    }
}