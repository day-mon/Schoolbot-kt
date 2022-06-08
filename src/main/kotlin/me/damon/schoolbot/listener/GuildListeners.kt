package me.damon.schoolbot.listener

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.objects.guild.GuildSettings
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.GuildService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import org.springframework.stereotype.Component

@Component
class GuildListeners(
    private val guildService: GuildService,
    private val schoolService: SchoolService,
    private val courseService: CourseService,
) : CoroutineEventListener
{
    private val logger by SLF4J

    fun onGuildJoinEvent(event: GuildJoinEvent)
    {
        try { guildService.save(GuildSettings(guildId = event.guild.idLong)) }
        catch (e: Exception) { logger.error("Error has occurred while trying to create guild settings in ({}) - [{}]", event.guild.name, event.guild.idLong) }
    }

    fun onGuildLeaveEvent(event: GuildLeaveEvent)
    {
        guildService.removeGuildInstance(event.guild.idLong)
    }

    suspend fun onRoleDeleteEvent(event: RoleDeleteEvent)
    {
        val guildId = event.guild.idLong
        val selfUser = event.jda.selfUser
        if (event.role.name == selfUser.name) return

        val logs = event.guild.retrieveAuditLogs()
            .type(ActionType.ROLE_DELETE)
            .limit(1)
            .await()

        val user = logs.first().user ?: return logger.error("Error while trying to obtain user")

        if (user.idLong == selfUser.idLong) return

        val roleDeleted = event.role.idLong
        val schoolRoles = try { schoolService.findSchoolsInGuild(guildId).map { MentionableDeleteDTO(it.roleId, it) } } catch (e: Exception) { return  logger.error("Error while trying to get schools in delete event") }
        val courseRoles = try { courseService.findAllByGuild(guildId).map { MentionableDeleteDTO(it.roleId, it) } } catch (e: Exception) { return  logger.error("Error while trying to get course in delete event") }
        val combined =  schoolRoles + courseRoles
        val found = combined.firstOrNull { it.mentionableId == roleDeleted } ?: return

        // I have to have duplicates right here because the compiler thinks they are identifiable
        when (val obj = found.obj)
        {
            is School -> schoolService.update( obj.apply { roleId = 0 } )
            is Course -> courseService.update( obj.apply { roleId = 0 } )
            else -> throw NotImplementedError("${obj.javaClass.name} has not been implemented yet")
        }
    }

    suspend fun onChannelDeleteEvent(event: ChannelDeleteEvent)
    {
        val selfUser = event.jda.selfUser

        val logs = event.guild.retrieveAuditLogs()
            .type(ActionType.CHANNEL_DELETE)
            .await()
        val user = logs.first().user ?: return  logger.error("Error while trying to obtain user")

        if (user.idLong == selfUser.idLong) return

        val channel = event.channel.idLong
        val schools = try { courseService.findAllByGuild(event.guild.idLong) } catch (e: Exception) { return logger.error("Error while trying to fetch schools") }
        val found = schools.filter { course -> course.channelId != 0L }
            .map { course -> MentionableDeleteDTO(course.channelId, course) }
            .find { it.mentionableId == channel } ?: return

        when (val obj = found.obj)
        {
            is Course -> courseService.update(obj.apply { channelId = 0L })
            else -> throw NotImplementedError("${obj.javaClass.name} has not been implemented yet")
        }
    }

    class MentionableDeleteDTO(val mentionableId: Long, val obj: Identifiable)

    override suspend fun onEvent(event: GenericEvent)  {
        when(event) {
            is ChannelDeleteEvent -> onChannelDeleteEvent(event)
            is RoleDeleteEvent -> onRoleDeleteEvent(event)
            is GuildJoinEvent -> onGuildJoinEvent(event)
            is GuildLeaveEvent -> onGuildLeaveEvent(event)
        }
    }
}