package me.damon.schoolbot.commands.sub.school.school

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceStringAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class SchoolEdit : SubCommand(
    name = "edit",
    description = "Edits a school",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "school_name",
            description = "Name of school you wish you edit",
            optionType = OptionType.STRING,
            isRequired = true,
            autoCompleteEnabled = true
        ),


    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val name = event.getOption<String>("school_name")

        val school = event.schoolbot.schoolService.findSchoolInGuild(
            name = name,
            guildId = event.guildId
        ) ?: return run {
            event.replyErrorEmbed("Error has occurred while trying to get schools or $name was deleted during/after the autocomplete process!")
        }

        // This could error if names are too large I assume
        val menu = SelectMenu("${event.slashEvent.idLong}_${school.id}:schoolEdit:menu")
        {
                option("Name - ${school.name}", "name")
                option("Url - ${school.url}", "url")
                option("Suffix - ${school.emailSuffix}", "suffix")
                option("Role - ${event.jda.getRoleById(school.roleId)?.name ?: "N/A"}", "role")
        }
        val selectionEvent = event.sendMenuAndAwait(
            menu = menu,
            message = "Please select the attribute you wish you edit"
        ) ?: return

        val choice = selectionEvent.values[0]


        val messageResponse: MessageReceivedEvent = evaluateMenuChoice(choice, event) ?: return run {
            event.replyErrorEmbed("This action is not yet implemented!")
        }

        val changedSchool = evaluateChangeRequest(event, messageResponse, choice, school) ?: return

        val updatedSchool = event.service.updateEntity(changedSchool) ?: return run {
            event.replyErrorEmbed("`${school.name}` either does not exist or an unexpected error occurred during the update sequence. Please try again. If this error persis please contact `damon#9999` ")
        }
        val embed = withContext(Dispatchers.IO) { updatedSchool.getAsEmbed() }

        event.replyEmbed(embed, "School Updated!")


    }

    private fun evaluateChangeRequest(event: CommandEvent, messageResponse: MessageReceivedEvent, choice: String, school: School): School?
    {
        val message = messageResponse.message.contentStripped
        return when (choice)
        {
            "name" ->
            {
                val duplicate = event.service.findDuplicateSchool(event.guildId, message) ?: return run {
                    event.replyErrorEmbed("Error occurred while trying to determine if $message is a duplicate school")
                    null
                }

                if (!duplicate) return run {
                    event.replyErrorEmbed("$message already exist as a school name")
                    null
                }

                 school.apply {
                    name = message
                }
            }

            "url" -> school.apply { url = message }
            "suffix" -> school.apply { emailSuffix = message }
            "role" ->
            {
                val oMessage = messageResponse.message

                if (oMessage.mentionedRoles.isEmpty() || message == "0") return run {
                    event.replyErrorEmbed("You did not mention any roles")
                    null
                }

                val roleId: Long = if (message == "0") 0L else oMessage.mentionedRoles[0].idLong

                if (roleId != 0L && roleId != school.roleId) return run {
                    val role = event.jda.getRoleById(roleId)?.asMention ?: return run {
                        event.replyErrorEmbed("Unexpected error")
                        null
                    }
                    event.replyErrorEmbed("$role is already `${school.name}'s` role")
                    null
                }

                if (roleId == 0L && school.roleId == 0L ) return run {
                    event.replyErrorEmbed("${school.name} already has no role")
                    null
                }

                 school.apply {
                    this.roleId = roleId
                }
            }
            else ->
            {
                logger.error("{} has not been implemented as a valid choice", choice)
                null
            }
        }
    }

    private suspend fun evaluateMenuChoice(choice: String, cmdEvent: CommandEvent) = when (choice) {
         "name" -> cmdEvent.sendMessageAndAwait("Please give me the new **name** you would like to call this school")
         "url" -> cmdEvent.sendMessageAndAwait("Please give me the new **url** you would like to school to go by")
         "suffix" -> cmdEvent.sendMessageAndAwait("Please give me the new **email suffix** you would like this school to by")
         "role" -> cmdEvent.sendMessageAndAwait("Please mention the **role** you would like this school to be mentioned by")
         else ->
         {
             logger.error("{} has not been implemented as a valid choice", choice)
             null
         }
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val schools: List<School> = schoolbot.schoolService.getSchoolsByGuildId(event.guild!!.idLong) ?: return run {
            logger.error("Was null")
        }
        event.replyChoiceStringAndLimit(
            schools.map { it.name }
        ).queue()
    }
}