package me.damon.schoolbot.commands.sub.school.professor

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
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
        val professorStringId = event.getOption("professor_name")
        val professorId = UUID.fromString(professorStringId!!.asString)


        val professor = event.service.findProfessorById(professorId) ?: return run {
            event.replyErrorEmbed("Professor not found. It must've been deleted during or before this command was executed. ${Emoji.THINKING.getAsChat()}")
        }


        event.replyMessage("Are you sure you want to remove ${professor.fullName}? ${Emoji.THINKING.getAsChat()}")


    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val professors =
            schoolbot.schoolService.findProfessorsByGuild(event.guild!!.idLong) ?: return
        event.replyChoiceAndLimit(
            professors
                .map { Command.Choice(it.fullName, it.id.toString()) }
                .filter { it.name.startsWith(event.focusedOption.value, ignoreCase = true) }
        ).queue()
    }
}