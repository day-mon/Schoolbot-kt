package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.sendPaginator
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import kotlin.time.Duration

class CourseView : SubCommand(
    name = "view",
    category = CommandCategory.SCHOOL,
    description = "Views a particular class or all class in your guild"
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val courses = event.schoolbot.schoolService.findCoursesByGuild(event.guild.idLong).onFailure {
            return run { event.replyErrorEmbed("Error has occurred while trying to get the courses for `${event.guild.name}`") }
        }.getOrDefault(setOf())

        if (courses.isEmpty()) return run { event.replyErrorEmbed("There are no courses in `${event.guild.name}`") }

        event.hook.sendPaginator(
            pages =  courses.map { it.getAsEmbed(event.guild) }.toTypedArray(),
            expireAfter = Duration.parse("5m")
        ) {
            it.user.idLong == event.slashEvent.user.idLong
        }.queue()
    }
}