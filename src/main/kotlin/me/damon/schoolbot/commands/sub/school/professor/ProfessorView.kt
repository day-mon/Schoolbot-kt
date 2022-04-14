package me.damon.schoolbot.commands.sub.school.professor

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.service.ProfessorService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType

class ProfessorView : SubCommand(
    name = "view",
    description = "Views all professors in a given school",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "professor_name",
            description = "Name of the school you wish to view professors from",
            optionType = OptionType.STRING,
            isRequired = true,
            autoCompleteEnabled = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val name = event.getOption<String>("professor_name")

        val service = event.getService<ProfessorService>()
        val professors = service.findProfessorsBySchool(name, event.guildId) ?: return
        event.sendPaginator(professors)
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val professors = schoolbot.schoolService.findProfessorsByGuild(event.guild!!.idLong)
            ?: return run { logger.error("Error occurred while trying to get professors") }


        event.replyChoiceAndLimit(
            professors.map { Command.Choice(it.fullName, it.id.toString()) }
        ).queue()
    }
}