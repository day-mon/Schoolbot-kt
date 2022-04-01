package me.damon.schoolbot.listener

import dev.minn.jda.ktx.SLF4J
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.guild.GuildSettings
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.GuildService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class GuildListeners(
    private val guildService: GuildService,
    private val schoolService: SchoolService,
) : ListenerAdapter()
{
    private val logger by SLF4J


    override fun onGuildJoin(event: GuildJoinEvent)
    {
        guildService.createSettings(
            GuildSettings(guildId = event.guild.ownerIdLong)
        ) ?: logger.error(
            "Error has occurred while trying to create guild settings in ({}) - [{}]",
            event.guild.name,
            event.guild.idLong
        )
    }

    override fun onGuildLeave(event: GuildLeaveEvent)
    {
        guildService.removeGuildInstance(event.guild.idLong)
    }

    override fun onRoleDelete(event: RoleDeleteEvent)
    {
        val selfUser = event.jda.selfUser
        if (event.role.name == selfUser.name) return

        event.guild.retrieveAuditLogs()
            .type(ActionType.ROLE_DELETE)
            .limit(1)
            .queue ({ logs ->
                val user = logs[0].user ?: return@queue run { logger.error("Error while trying to obtain user") }

                if (user.idLong == selfUser.idLong) return@queue

                val roleDeleted = event.role.idLong
                val schoolRoles = schoolService.getSchoolsByGuildId(event.guild.idLong)?.map { MentionableDeleteDTO(it.roleId, it) } ?: return@queue run { logger.error("Error while trying to get schools in delete event") }
                val courseRoles = schoolService.getClassesInGuild(event.guild.idLong)?.map { MentionableDeleteDTO(it.roleId, it) } ?: return@queue run { logger.error("Error while trying to get course in delete event") }
                val combined = schoolRoles.plus(courseRoles)
                val found = combined.firstOrNull { it.mentionableId == roleDeleted } ?: return@queue

                // I have to have duplicates right here because the compiler thinks they are identifiable
                when (val obj = found.obj)
                {
                    is School -> schoolService.updateEntity( obj.apply { roleId = 0 } ) ?: return@queue
                    is Course -> schoolService.updateEntity( obj.apply { roleId = 0 } ) ?: return@queue
                    else -> TODO("${obj.javaClass.name} has not been implemented yet")
                }
            }) { failure -> logger.error("Error has occurred while trying to retrieve the audit logs", failure) }
    }

    override fun onChannelDelete(event: ChannelDeleteEvent)
    {
        val selfUser = event.jda.selfUser

        event.guild.retrieveAuditLogs()
            .type(ActionType.CHANNEL_DELETE)
            .queue ({ logs ->
                val user = logs[0].user ?: return@queue run { logger.error("Error while trying to obtain user") }

                if (user.idLong == selfUser.idLong) return@queue

                val channel = event.channel.idLong
                val schools = schoolService.getClassesInGuild(event.guild.idLong) ?: return@queue run { logger.error("Error while trying to fetch schools") }
                val found = schools.filter { course -> course.channelId != 0L }
                    .map { course -> MentionableDeleteDTO(course.channelId, course) }
                    .find { it.mentionableId == channel } ?: return@queue

                when (val obj = found.obj)
                {
                    is Course -> schoolService.updateEntity(obj.apply { channelId = 0L })
                    else -> TODO("${obj.javaClass.name} has not been implemented yet")
                }
            }) { failure -> logger.error("Error has occurred while trying to retrieve the audit logs", failure) }
    }

    class MentionableDeleteDTO(val mentionableId: Long, val obj: Identifiable)
}