package me.damon.schoolbot.commands.sub.school.professor

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceStringAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.service.ProfessorService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class ProfessorAdd : SubCommand(
    name = "add", description = "Adds a professor to a school", category = CommandCategory.SCHOOL, options = listOf(
        CommandOptionData<String>(
            name = "first_name",
            description = "First name of the professor",
            optionType = OptionType.STRING,
            isRequired = true

        ),

        CommandOptionData(
            name = "last_name",
            description = "Last name of the professor",
            optionType = OptionType.STRING,
            isRequired = true
        ),

        CommandOptionData(
            name = "school_name",
            description = "Name of the school",
            isRequired = true,
            optionType = OptionType.STRING,
            autoCompleteEnabled = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val firstName = event.getOption<String>("first_name")
        val lastName = event.getOption<String>("last_name")
        val schoolName = event.getOption<String>("school_name")
        val professorService = event.getService<ProfessorService>()

        val service = event.service
        val school = service.findSchoolInGuild(event.guildId, schoolName)
            ?: return run { event.replyErrorEmbed("Error occurred while trying to get school or school does not exist") }

        val professor = Professor(
            firstName = firstName, lastName = lastName, school = school
        )

        val prof = professorService.findProfessorByName(professor.fullName, school)
        if (prof.isEmpty) return run { event.replyErrorEmbed("Error occurred during command runtime") }


        val savedProfessor = service.saveProfessor(professor) ?: return run {
            event.replyErrorEmbed("Error occurred while trying to save professor")
        }

        event.replyEmbed(
            embed = savedProfessor.getAsEmbed(), content = "Professor $lastName has been saved"
        )
    }


    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val schools = schoolbot.schoolService.getSchoolsByGuildId(event.guild!!.idLong) ?:
            return run { logger.error("Error occurred during auto complete in Professor Add command") }

        event.replyChoiceStringAndLimit(
            schools.map { it.name }
        ).queue()
    }
}