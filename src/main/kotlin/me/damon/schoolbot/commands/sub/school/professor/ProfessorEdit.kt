package me.damon.schoolbot.commands.sub.school.professor

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.service.ProfessorService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

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
        val schoolId = event.getOption<String>("school_name")
        val service = event.getService<ProfessorService>()
        val schoolUUID = UUID.fromString(schoolId)

        val professors = try { service.findBySchoolId(schoolUUID).toMutableList()  }
        catch (e: Exception) { return run { event.replyErrorEmbed("Error occurred while trying to fetch professors by school.") } }

        val selection =  event.sendMenuAndAwait(
            menu = SelectMenu("${event.slashEvent.id}_${event.user.idLong}:SM:EDIT_PROFESSOR")
            { professors.forEachIndexed { index, professor -> option(professor.fullName, index.toString()) } },

            message = "Select a professor to edit",
         ) ?: return

        val professor = professors[selection.values[0].toInt()]

        val menu = SelectMenu("${event.slashEvent.id}_${event.user.idLong}:SM:EDIT_PROFESSOR_${professor.id}")
        {
            option("First name - ${professor.firstName} ", "first_name")
            option("Last name - ${professor.lastName}", "last_name")
            option("Email Prefix - ${professor.emailPrefix}", "email_prefix")
        }

        val finalSelection = event.sendMenuAndAwait(
            menu = menu,
            message = "Select a field to edit",
        ) ?: return

        val choice = finalSelection.values[0]

        val messageResponse: MessageReceivedEvent = try {
            evaluateMenuChoice(choice, event)
        } catch (e: NotImplementedError) { return run {
            event.replyErrorEmbed("This action is not yet implemented!")
        } }

        val changedProfessor = evaluateChangeRequest(event, messageResponse, choice, professor) ?: return


        val updatedProfessor = event.service.updateEntity(changedProfessor) ?: return run {
            event.replyErrorEmbed("An unexpected error has occurred while trying to save the entity")
        }

        val embed = withContext(Dispatchers.IO) { updatedProfessor.getAsEmbed() }

        event.replyEmbed(embed, "Professor Updated!")
    }

    private suspend fun evaluateMenuChoice(choice: String, cmdEvent: CommandEvent): MessageReceivedEvent = when (choice) {
        "first_name" -> cmdEvent.sendMessageAndAwait("Please give me the new **First Name** you would like to call this professor")
        "last_name" -> cmdEvent.sendMessageAndAwait("Please give me the new **Last Name** you would like to professor to go by")
        "prefix" -> cmdEvent.sendMessageAndAwait("Please give me the new **Email Prefix** you would like this professor to by")
        else ->
        {
            logger.error("{} has not been implemented as a valid choice", choice)
            throw NotImplementedError("$choice has not been implemented as a valid choice")
        }
    }

    private fun evaluateChangeRequest(event: CommandEvent, messageResponse: MessageReceivedEvent, choice: String, professor: Professor): Professor?
    {
        val message = messageResponse.message.contentStripped
        val professorService = event.getService<ProfessorService>()
        return when (choice)
        {
            "first_name" ->{
                val duplicate = professorService.findByNameInSchool("$message ${professor.lastName}", professor.school)
                if (duplicate.isPresent) return run {
                    event.replyErrorEmbed("Professor with this name already exists")
                    null
                }
                professor.apply {
                    firstName = message
                    fullName = "$message $lastName"
                }
            }
            "last_name" -> {
                val duplicateProfessor =  professorService.findByNameInSchool("${professor.firstName} $message", professor.school)
                if (duplicateProfessor.isPresent) return run {
                    event.replyErrorEmbed("Professor with this name already exists")
                    null
                }
                professor.apply {
                    lastName = message
                    fullName = "$firstName $message"
                }

            }
            "prefix" -> { professor.apply { emailPrefix = message } }
            else ->
            {
                logger.error("{} has not been implemented as a valid choice", choice)
                null
            }
        }
    }


    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val schools = schoolbot.schoolService.getSchoolsWithProfessorsInGuild(event.guild!!.idLong) ?: return

        event.replyChoiceAndLimit(
            schools.map { Command.Choice(it.name, it.id.toString()) }
        ).queue()
    }
}