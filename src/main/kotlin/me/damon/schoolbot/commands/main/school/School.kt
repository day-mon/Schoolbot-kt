package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.SchoolAdd
import me.damon.schoolbot.commands.sub.school.SchoolEdit
import me.damon.schoolbot.commands.sub.school.SchoolRemove
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent

class School : Command(
    name = "School",
    category = CommandCategory.SCHOOL,
    description = "Allows you to add/remove/edit schools in the server",
    children = listOf(
        SchoolAdd(),
        SchoolEdit(),
        SchoolRemove()
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent){}
}