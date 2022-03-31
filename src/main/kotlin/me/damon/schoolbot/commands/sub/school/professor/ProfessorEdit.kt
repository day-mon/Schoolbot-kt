package me.damon.schoolbot.commands.sub.school.professor

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.ext.toUUID
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.Professor
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.Command
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
        val schoolId = event.getOption<String>("school_name")
        val id = schoolId.toUUID() ?: return run { event.replyErrorEmbed("An unexpected error has occurred") }
        val school = event.service.findSchoolById(id) ?: return run { event.replyErrorEmbed("Error occurred while trying to fetch school by id. ${Emoji.THINKING.getAsChat()}") }

        if (school.isEmpty) return run { event.replyErrorEmbed("School has not been found") }

        val finalSchool = school.get()

        val professors = event.service.findProfessorsBySchool(finalSchool)?.toMutableList() ?: return run { event.replyErrorEmbed("Error occurred while trying to fetch professors by school.")}

        val selection =  event.sendMenuAndAwait(
            menu = SelectMenu("${event.slashEvent.id}_${event.user.idLong}:SM:EDIT_PROFESSOR")
            { professors.forEachIndexed { index, professor -> option(professor.fullName, index.toString()) } },

            message = "Select a professor to edit",
         )

        val professor = professors[selection.values[0].toInt()]

        val menu = SelectMenu("${event.slashEvent.id}_${event.user.idLong}:SM:EDIT_PROFESSOR_${professor.id}")
        {
            option("First name - ${professor.firstName} ", "first_name")
            option("Last name - ${professor.lastName}", "last_name")
            option("Email Prefix - ${professor.emailPrefix}", "prefix")
        }

        val finalSelection = event.sendMenuAndAwait(
            menu = menu,
            message = "Select a field to edit",
        )

        val choice = finalSelection.values[0]

        val messageResponse: MessageReceivedEvent = evaluateMenuChoice(choice, event) ?: return run {
            event.replyErrorEmbed("This action is not yet implemented!")
        }

        val changedProfessor = evaluateChangeRequest(event, messageResponse, choice, professor) ?: return


        val updatedProfessor = event.service.updateEntity(changedProfessor) ?: return run {
            event.replyErrorEmbed("An unexpected error has occurred whiel trying to save the entitiy")
        }

        val embed = withContext(Dispatchers.IO) { updatedProfessor.getAsEmbed() }

        event.replyEmbed(embed, "Professor Updated!")
    }

    private suspend fun evaluateMenuChoice(choice: String, cmdEvent: CommandEvent) = when (choice) {
        "first_name" -> cmdEvent.sendMessageAndAwait("Please give me the new **First Name** you would like to call this professor")
        "last_name" -> cmdEvent.sendMessageAndAwait("Please give me the new **Last Name** you would like to professor to go by")
        "prefix" -> cmdEvent.sendMessageAndAwait("Please give me the new **Email Prefix** you would like this professor to by")
        else ->
        {
            logger.error("{} has not been implemented as a valid choice", choice)
            null
        }
    }

    private fun evaluateChangeRequest(event: CommandEvent, messageResponse: MessageReceivedEvent, choice: String, professor: Professor): Professor?
    {
        val message = messageResponse.message.contentStripped
        return when (choice)
        {
            "first_name" ->{
                val duplicate = event.service.findProfessorByName("$message ${professor.lastName}", professor.school)
                if (duplicate != null) return run {
                    event.replyErrorEmbed("Professor with this name already exists")
                    null
                }
                professor.apply {
                    firstName = message
                    fullName = "$message $lastName"
                }
            }
            "last_name" -> {
                val duplicate =  event.service.findProfessorByName("${professor.firstName} $message", professor.school)
                if (duplicate != null) return run {
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