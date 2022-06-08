package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.school.SchoolAdd
import me.damon.schoolbot.commands.sub.school.school.SchoolEdit
import me.damon.schoolbot.commands.sub.school.school.SchoolRemove
import me.damon.schoolbot.commands.sub.school.school.SchoolView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import org.springframework.stereotype.Component

@Component
class School(
    schoolAdd: SchoolAdd,
    schoolEdit: SchoolEdit,
    schoolRemove: SchoolRemove,
    schoolView: SchoolView
) : Command(
    name = "School",
    category = CommandCategory.SCHOOL,
    description = "Allows you to add/remove/view/edit schools in the server",
    children = listOf(
        schoolAdd,
        schoolEdit,
        schoolRemove,
        schoolView
    )
)