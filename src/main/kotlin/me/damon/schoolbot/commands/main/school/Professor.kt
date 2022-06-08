package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.professor.ProfessorAdd
import me.damon.schoolbot.commands.sub.school.professor.ProfessorEdit
import me.damon.schoolbot.commands.sub.school.professor.ProfessorRemove
import me.damon.schoolbot.commands.sub.school.professor.ProfessorView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import org.springframework.stereotype.Component

@Component
class Professor(
    professorAdd: ProfessorAdd,
    professorView: ProfessorView,
    professorEdit: ProfessorEdit,
    professorRemove: ProfessorRemove
) : Command(
    name = "Professor",
    description = "Allows to you to add/remove/edit professor in the server",
    category = CommandCategory.SCHOOL,
    children = listOf(
        professorAdd,
        professorEdit,
        professorView,
        professorRemove
    ),
)