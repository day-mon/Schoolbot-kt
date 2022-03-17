package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.course.CourseAddNormal
import me.damon.schoolbot.commands.sub.school.course.CourseAddPitt
import me.damon.schoolbot.commands.sub.school.course.CourseRemove
import me.damon.schoolbot.commands.sub.school.course.CourseView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory

class Course : Command(
    name = "Course",
    category = CommandCategory.SCHOOL,
    description = "Allows you to add/remove/view/edit a course",
    group = mapOf(
        "add" to listOf(
            CourseAddNormal(), CourseAddPitt()
        ),
        /*
        "pitt" to listOf(
            CoursePittInfo()
        )
         */
    ),
    children = listOf(
        CourseView(),
        CourseRemove()
    )
)