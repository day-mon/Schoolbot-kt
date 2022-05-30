package me.damon.schoolbot.commands.sub.school.professor

import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.into
import me.damon.schoolbot.bot.Schoolbot
import me.damon.schoolbot.ext.*
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
        val schoolId = event.getOption<String>("school_name").toUUID()
            ?: return event.replyErrorEmbed("School with that name has not been found")
        val service = event.getService<ProfessorService>()

        val professors = try { service.findBySchoolId(schoolId) }
        catch (e: Exception) { return event.replyErrorEmbed("Error occurred while trying to fetch professors by school.") }

        val professorMenu = SelectMenu("professor_edit") {
            professors
                .forEachIndexed { index, professor -> option(professor.fullName, index.toString()) }
        }

        val selection = event.awaitMenu(
            menu = professorMenu,
            message = "Select a professor to edit",
            deleteAfter = true
        ) ?: return

        val professor = professors[selection.values.first().toInt()]

        val modal = Modal(
            id = "professor_edit_modal",
            title = "Editing ${professor.fullName}"
        ) {
            short(id = "first", label = "First Name", value = professor.firstName, required = true)
            short(id = "last", label = "Last Name", value = professor.lastName, required = true)
            short(id = "email", label = "Email Prefix", value = professor.emailPrefix, required = true)
        }

        val modalEvent = selection.awaitModal(
            modal = modal
        ) ?: return

        selection.editComponents(professorMenu.asDisabled().into()).queue()

        val firstName = modalEvent.getValue<String>("first")
            ?: return event.replyErrorEmbed(error = "First name field cannot be empty")
        val lastName  = modalEvent.getValue<String>("last")
            ?: return event.replyErrorEmbed(error = "Last name field cannot be empty")
        val email     = modalEvent.getValue<String>("email")
            ?: return event.replyErrorEmbed(error = "Email field cannot be empty")


        val duplicateProfessor = service
            .findByNameInSchool("$firstName $lastName", professor.school)

        if (duplicateProfessor != null && professor.fullName != duplicateProfessor.fullName) return modalEvent.replyErrorEmbed("Professor with the name $firstName $lastName already exist").queue()

        professor.apply {
            this.firstName = firstName
            this.lastName = lastName
            this.fullName = "$firstName $lastName"
            this.emailPrefix = email
        }

        val professorSaved = try { service.save(professor) }
        catch (e: Exception) { return modalEvent.replyErrorEmbed("Failed to save ${professor.fullName} ").queue() }

        modalEvent.replyEmbeds(professorSaved.getAsEmbed()).queue()
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