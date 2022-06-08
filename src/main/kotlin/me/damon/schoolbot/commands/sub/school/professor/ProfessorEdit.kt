package me.damon.schoolbot.commands.sub.school.professor

import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.into
import me.damon.schoolbot.ext.*
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.service.ProfessorService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.stereotype.Component

@Component
class ProfessorEdit(
    private val schoolService: SchoolService,
    private val professorService: ProfessorService
) : SubCommand(
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

        val professors = try { professorService.findBySchoolId(schoolId) }
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


        val duplicateProfessor = professorService
            .findByNameInSchool("$firstName $lastName", professor.school)

        if (duplicateProfessor != null && professor.fullName != duplicateProfessor.fullName) return modalEvent.replyErrorEmbed("Professor with the name $firstName $lastName already exist").queue()

        professor.apply {
            this.firstName = firstName
            this.lastName = lastName
            this.fullName = "$firstName $lastName"
            this.emailPrefix = email
        }

        val professorSaved = try { professorService.save(professor) }
        catch (e: Exception) { return modalEvent.replyErrorEmbed("Failed to save ${professor.fullName} ").queue() }

        modalEvent.replyEmbeds(professorSaved.getAsEmbed()).queue()
    }




    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)

    {
        val guildId = event.guild?.idLong ?: return logger.error("Guild is null")
        val schools = schoolService.findByEmptyProfessors(guildId)
        event.replyChoiceAndLimit(schools.map { Command.Choice(it.name, it.id.toString()) }).queue()
    }
}