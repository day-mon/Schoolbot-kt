package me.damon.schoolbot.commands.sub.school.professor

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.service.ProfessorService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

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
        val professorStringId = event.getOption<String>("professor_name")
        val service = event.getService<ProfessorService>()
        val professorId = UUID.fromString(professorStringId)


        val professorOptional = service.findProfessorById(professorId)


        if (professorOptional.isEmpty) return run {
            event.replyErrorEmbed("Professor not found. It must've been deleted during or before this command was executed. ${Emoji.THINKING.getAsChat()}")
        }

        val professor = professorOptional.get()



        event.replyMessage("Are you sure you want to remove ${professor.fullName}? ${Emoji.THINKING.getAsChat()}")


    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val service = schoolbot.professorService
        val guild = event.guild ?: return // shouldn't be possible unless handler is broken
        val professors = service.findProfessorsByGuild(guild.idLong)
        event.replyChoiceAndLimit(
            professors
                .map { Command.Choice(it.fullName, it.id.toString()) }
        ).queue()
    }
}