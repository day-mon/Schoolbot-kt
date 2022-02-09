package me.damon.schoolbot.commands.sub.school

import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.repository.SchoolRepository

class SchoolEdit : SubCommand(
    name = "edit",
    description = "Edits a school",
    category = CommandCategory.SCHOOL
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        TODO("Not yet implemented")
    }
}