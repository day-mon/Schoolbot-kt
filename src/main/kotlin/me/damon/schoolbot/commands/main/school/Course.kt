package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.CourseAddNormal
import me.damon.schoolbot.commands.sub.school.CourseAddPitt
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory

class Course : Command(
    name = "Course",
    category = CommandCategory.SCHOOL,
    description = "Allows you to add/remove/edit a course",
    group = mapOf("add" to listOf(
         CourseAddNormal(),
         CourseAddPitt())
    )
)