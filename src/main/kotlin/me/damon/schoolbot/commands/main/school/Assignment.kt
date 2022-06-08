package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.assignment.AssignmentAdd
import me.damon.schoolbot.commands.sub.school.assignment.AssignmentEdit
import me.damon.schoolbot.commands.sub.school.assignment.AssignmentRemove
import me.damon.schoolbot.commands.sub.school.assignment.AssignmentView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import org.springframework.stereotype.Component

@Component
class Assignment(
    assignmentAdd: AssignmentAdd,
    assignmentEdit: AssignmentEdit,
    assignmentView: AssignmentView,
    assignmentRemove: AssignmentRemove
) : Command(
    name = "Assignment",
    description = "Allows you to add/remove/view assignments.",
    category = CommandCategory.SCHOOL,

)
