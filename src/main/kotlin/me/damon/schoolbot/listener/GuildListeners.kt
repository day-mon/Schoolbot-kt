package me.damon.schoolbot.listener

import dev.minn.jda.ktx.SLF4J
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.guild.GuildSettings
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildListeners(val schoolbot: Schoolbot) : ListenerAdapter()
{
    private val logger by SLF4J

    override fun onGuildJoin(event: GuildJoinEvent)
    {
        schoolbot.guildService.createSettings(
            GuildSettings(guildId = event.guild.ownerIdLong)
        ) ?: logger.error(
            "Error has occurred while trying to create guild settings in ({}) - [{}]",
            event.guild.name,
            event.guild.idLong
        )
    }

    override fun onGuildLeave(event: GuildLeaveEvent)
    {
        schoolbot.guildService.removeGuildInstance(event.guild.idLong)
    }

    override fun onRoleDelete(event: RoleDeleteEvent)
    {
        val roleDeleted = event.role.idLong
        val schoolRoles = schoolbot.schoolService.getSchoolsByGuildId(event.guild.idLong)?.map { MentionableDeleteDTO(it.roleId, it) } ?: return run { logger.error("Error while trying to get schools in delete event") }
        val courseRoles = schoolbot.schoolService.getClassesInGuild(event.guild.idLong)?.map { MentionableDeleteDTO(it.roleId, it) } ?: return run { logger.error("Error while trying to get course in delete event") }
        val combined = schoolRoles.plus(courseRoles)
        val found = combined.firstOrNull { it.mentionableId == roleDeleted } ?: return



        // I have to have duplicates right here because the compiler thinks they are identifiable
        when (val obj = found.obj)
        {
            is School -> schoolbot.schoolService.updateEntity( obj.apply { roleId = 0 } ) ?: return
            is Course -> schoolbot.schoolService.updateEntity( obj.apply { roleId = 0 } ) ?: return
            else -> TODO("${obj.javaClass.name} has not been implemented yet")
        }
    }

    override fun onChannelDelete(event: ChannelDeleteEvent)
    {
        val channel = event.channel.idLong
        val schools = schoolbot.schoolService.getClassesInGuild(event.guild.idLong) ?: return run { logger.error("Error while trying to fetch schools") }
        val found = schools.filter { course -> course.channelId != 0L  }
            .map { course -> MentionableDeleteDTO(course.channelId, course) }
            .find { it.mentionableId == channel } ?: return

        when (val obj = found.obj)
        {
            is Course -> schoolbot.schoolService.updateEntity( obj.apply { channelId = 0L } )
            else -> TODO("${obj.javaClass.name} has not been implemented yet")
        }
    }

    class MentionableDeleteDTO(val mentionableId: Long, val obj: Identifiable)
}