package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.course.*
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory

class Course : Command(
    name = "Course",
    category = CommandCategory.SCHOOL,
    description = "Allows you to add/remove/view/edit a course",
    group = mapOf(
        "add" to listOf(
            CourseAddNormal(),
            CourseAddPitt()
        ),
    ),
    children = listOf(
        CourseView(),
        CourseRemove(),
        CourseEdit()
    )
)