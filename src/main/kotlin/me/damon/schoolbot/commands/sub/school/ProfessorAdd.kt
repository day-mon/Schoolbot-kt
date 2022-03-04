package me.damon.schoolbot.commands.sub.school

import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand

class ProfessorAdd : SubCommand(
    name = "add",
    description = "Adds a professor to a school",
    category = CommandCategory.SCHOOL,
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
    }
}