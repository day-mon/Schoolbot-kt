package me.damon.schoolbot.commands.sub.school.course

import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.service.CourseService

class CourseView : SubCommand(
    name = "view",
    category = CommandCategory.SCHOOL,
    description = "Views a particular class or all class in your guild"
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val service = event.getService<CourseService>()
        val courses = try { service.findAllByGuild(event.guild.idLong) } catch (e: Exception) {
             event.replyErrorEmbed("Error has occurred while trying to get the courses for `${event.guild.name}`")
            return
        }

        if (courses.isEmpty()) return run { event.replyErrorEmbed("There are no courses in `${event.guild.name}`") }

        event.sendPaginatorColor(courses)

    }
}