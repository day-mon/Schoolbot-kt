package me.damon.schoolbot.commands.sub.school.professor

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class ProfessorEdit : SubCommand(
    name = "edit",
    category = CommandCategory.SCHOOL,
    description = "Edits a professor",
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "school_name",
            description = "School name",
            isRequired = true,
            autoCompleteEnabled = true
        )
    )
)
{

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
       // val schoolName = event.getOption("school_name")!!.asString



    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val schools = schoolbot.schoolService.getSchoolsByGuildId(event.guild!!.idLong) ?: return
        /*
        event.replyChoiceStrings(
            schools.filter { it.hasProfessors() }
                .map { it.name }
                .filter { it.startsWith(event.focusedOption.value, ignoreCase = true) }
                .take(25)
        ).queue()

         */
    }
}