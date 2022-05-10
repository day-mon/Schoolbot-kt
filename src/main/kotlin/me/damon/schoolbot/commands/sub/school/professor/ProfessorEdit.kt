package me.damon.schoolbot.commands.sub.school.professor

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option
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
import java.util.concurrent.TimeoutException

class ProfessorEdit : SubCommand(
    name = "edit", category = CommandCategory.SCHOOL, description = "Edits a professor", options = listOf(
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

        val professors = try
        {
            service.findBySchoolId(schoolUUID).toMutableList()
        } catch (e: Exception)
        {
            return run { event.replyErrorEmbed("Error occurred while trying to fetch professors by school.") }
        }

        val selection = event.awaitMenu(
            menu = SelectMenu("${event.slashEvent.id}_${event.user.idLong}:SM:EDIT_PROFESSOR") {
                professors.forEachIndexed { index, professor ->
                    option(
                        professor.fullName, index.toString()
                    )
                }
            },

            message = "Select a professor to edit",
        ) ?: return

        val professor = professors[selection.values[0].toInt()]

        val menu = SelectMenu("${event.slashEvent.id}_${event.user.idLong}:SM:EDIT_PROFESSOR_${professor.id}") {
            option("First name - ${professor.firstName} ", "first_name")
            option("Last name - ${professor.lastName}", "last_name")
            option("Email Prefix - ${professor.emailPrefix}", "email_prefix")
        }

        val finalSelection = event.awaitMenu(
            menu = menu,
            message = "Select a field to edit",
        ) ?: return

        val choice = finalSelection.values[0]

        val messageResponse: MessageReceivedEvent = try
        {
            evaluateMenuChoice(choice, event)
        }
        catch (e: NotImplementedError)
        {
            event.replyErrorEmbed("This action is not yet implemented!")
            return
        }
        catch (e: TimeoutException)
        {
            event.replyErrorEmbed("You took too long to respond!")
            return
        }

        val changedProfessor = try { evaluateChangeRequest(event, messageResponse, choice, professor) } catch (e: NotImplementedError)
        {
            event.replyErrorEmbed(e.message ?: "An error occurred while trying to edit the professor.")
            return
        }
        catch (e: Exception)
        {
            event.replyErrorEmbed(e.message ?: "An error occurred while trying to edit the professor.")
            return
        } ?: return


        val updatedProfessor = try
        {
            service.save(changedProfessor)
        }
        catch (e: Exception)
        {
            event.replyErrorEmbed("An unexpected error has occurred while trying to save the entity")
            return
        }

        val embed = withContext(Dispatchers.IO) { updatedProfessor.getAsEmbed() }

        event.replyEmbed(embed, "Professor Updated!")
    }

    private suspend fun evaluateMenuChoice(choice: String, cmdEvent: CommandEvent): MessageReceivedEvent = when (choice)
    {
        "first_name" -> cmdEvent.sendMessageAndAwait("Please give me the new **First Name** you would like to call this professor")
        "last_name" -> cmdEvent.sendMessageAndAwait("Please give me the new **Last Name** you would like to professor to go by")
        "prefix" -> cmdEvent.sendMessageAndAwait("Please give me the new **Email Prefix** you would like this professor to by")
        else ->
        {
            logger.error("{} has not been implemented as a valid choice", choice)
            throw NotImplementedError("$choice has not been implemented as a valid choice")
        }
    }

    private suspend fun evaluateChangeRequest(
        event: CommandEvent, messageResponse: MessageReceivedEvent, choice: String, professor: Professor
    ): Professor?
    {
        val message = messageResponse.message.contentStripped
        val professorService = event.getService<ProfessorService>()
        return when (choice)
        {
            "first_name", "last_name" ->
            {
                val name = if (choice == "first_name") "$message ${professor.lastName}" else "${professor.firstName} $message"
                val duplicate = try
                {
                    professorService.findByNameInSchool("$message ${professor.lastName}", professor.school)
                }
                catch (e: Exception)
                {
                    event.replyErrorEmbed("An unexpected error has occurred while trying to find the professor")
                    return null
                }

                if (duplicate != null)
                {
                    event.replyErrorEmbed("Professor with this name already exists")
                    return null
                }

                professor.apply {
                    if (choice == "first_name") firstName = message else lastName = message
                    fullName = name
                }
            }
            "prefix" ->
            {
                professor.apply { emailPrefix = message }
            }
            else ->
            {
                logger.error("{} has not been implemented as a valid choice", choice)
                throw NotImplementedError("$choice has not been implemented as a valid choice")
            }
        }
    }


    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val guildId = event.guild?.idLong ?: return logger.error("Guild is null")
        val schools = schoolbot.schoolService.findByEmptyProfessors(guildId)
        event.replyChoiceAndLimit(schools.map { Command.Choice(it.name, it.id.toString()) }).queue()
    }
}