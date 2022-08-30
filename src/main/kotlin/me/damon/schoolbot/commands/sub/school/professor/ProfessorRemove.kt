package me.damon.schoolbot.commands.sub.school.professor

import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.into

import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.ext.send
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.service.ProfessorService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Component
class ProfessorRemove(
    private val professorService: ProfessorService,
) : SubCommand(
    name = "remove",
    description = "Removes a professor",
    category = CommandCategory.SCHOOL,
    options = listOf(
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
        val professorId = UUID.fromString(professorStringId)


        val professor = try { professorService.findById(professorId) }
        catch (e: Exception) { return event.replyErrorEmbed("An unexpected error has occurred") }
            ?: return event.replyErrorEmbed("Professor not found. It must've been deleted during or before this command was executed. ${Emoji.THINKING.getAsChat()}")


        event.slashEvent.send("Are you sure you want to remove ${professor.fullName}? ${Emoji.THINKING.getAsChat()}", actionRows = getActionRows(event, professor))


    }

    fun getActionRows(event: CommandEvent, professor: Professor): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {
            it.message.edit("Removing professor...", components = listOf())
            try
            {
                professorService.remove(professor)
            }
            catch (e: Exception)
            {
               return@button it.message.edit("An unexpected error has occurred").queue()
            }

            it.message.edit("Professor removed successfully").queue()
        }

        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = event.user, expiration = 1.minutes) {
            it.message.edit("Professor will not be removed", components = listOf()).queue()
        }

        return listOf(yes, no)
    }


    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)

    {
        val guild = event.guild ?: return // shouldn't be possible unless handler is broken
        val professors = professorService.findAllInGuild(guild.idLong)
        event.replyChoiceAndLimit(professors.map { Command.Choice(it.fullName, it.id.toString()) }).queue()
    }
}