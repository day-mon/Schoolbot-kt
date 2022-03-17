package me.damon.schoolbot.commands.sub.school.course

import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand

class CourseAddNormal : SubCommand(
    name = "normal",
    description = "Adds a course",
    category = CommandCategory.SCHOOL,
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        super.onExecuteSuspend(event)
    }
}