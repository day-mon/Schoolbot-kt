package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.course.*
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import org.springframework.stereotype.Component

@Component
class Course(
    courseAddNormal: CourseAddNormal,
    courseAddPitt: CourseAddPitt,
    courseView: CourseView,
    courseRemove: CourseRemove,
    courseEdit: CourseEdit
) : Command(
    name = "Course",
    category = CommandCategory.SCHOOL,
    description = "Allows you to add/remove/view/edit a course",
    group = mapOf(
        "add" to listOf(
            courseAddNormal,
            courseAddPitt
        ),
    ),
    children = listOf(
        courseView,
        courseRemove,
        courseEdit
    )
)