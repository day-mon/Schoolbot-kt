package me.damon.schoolbot.listener

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.guild.GuildSettings
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildListeners(val schoolbot: Schoolbot) : ListenerAdapter()
{
    override fun onGuildJoin(event: GuildJoinEvent)
    {
        schoolbot.guildService.createSettings(
            GuildSettings(guildId = event.guild.ownerIdLong)
        )
    }

    override fun onGuildLeave(event: GuildLeaveEvent)
    {
        schoolbot.guildService.removeGuildInstance(event.guild.idLong)
    }
}