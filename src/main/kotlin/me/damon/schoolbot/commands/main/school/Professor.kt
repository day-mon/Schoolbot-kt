package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.professor.ProfessorAdd
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory

class Professor : Command(
    name = "Professor",
    description = "Allows to you to add/remove/edit professor in the server",
    category = CommandCategory.SCHOOL,
    children = listOf(
        ProfessorAdd()
    ),
)