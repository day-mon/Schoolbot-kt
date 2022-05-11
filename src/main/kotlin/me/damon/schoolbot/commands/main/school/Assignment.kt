package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.assignment.AssignmentAdd
import me.damon.schoolbot.commands.sub.school.assignment.AssignmentEdit
import me.damon.schoolbot.commands.sub.school.assignment.AssignmentRemove
import me.damon.schoolbot.commands.sub.school.assignment.AssignmentView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory

class Assignment : Command(
    name = "Assignment",
    description = "Allows you to add/remove/view assignments.",
    category = CommandCategory.SCHOOL,
    children = listOf(
        AssignmentAdd(),
        AssignmentRemove(),
        AssignmentView(),
       // AssignmentEdit()
    )
)
{}