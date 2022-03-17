package me.damon.schoolbot.commands.sub.school.professor

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class ProfessorRemove : SubCommand(
    name = "remove", description = "Removes a professor", category = CommandCategory.SCHOOL, options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "professor_name",
            isRequired = true,
            description = "Name of the professor you want to remove",
            autoCompleteEnabled = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {

    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        if (event.focusedOption.name == "school_name")
        {
            val schools = schoolbot.schoolService.getSchoolsByGuildId(event.guild!!.idLong) ?: return
            event.replyChoiceStrings(schools.map { it.name }).queue()
        }
        else
        {
            // not null assertions are just for testing
            val schoolStr = event.getOption("school_name")!!.asString
            val school = schoolbot.schoolService.findSchoolInGuild(event.guild!!.idLong, schoolStr)
            val professor = schoolbot.schoolService.getProfessorsInSchool(school!!)
            event.replyChoiceStrings(professor!!.fullName).queue()
        }
    }
}