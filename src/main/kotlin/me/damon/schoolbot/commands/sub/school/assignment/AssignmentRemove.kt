package me.damon.schoolbot.commands.sub.school.assignment

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.into

import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.ext.send
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.service.AssignmentService
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Component
class AssignmentRemove(
    private val schoolService: SchoolService,
    private val courseService: CourseService,
    private val assignmentService: AssignmentService
) : SubCommand(
    name = "remove",
    description = "Removes an assignment from the course.",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "school",
            description = "The school to remove the assignment from.",
            isRequired = true,
            optionType = OptionType.STRING,
            autoCompleteEnabled = true

        )
    )
)
{

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val uuidStr = event.getOption<String>("school")
        val uuid = try { UUID.fromString(uuidStr) } catch (e: Exception) { return event.replyErrorEmbed("That school is not available for deletion at this time") }
        val school = try { schoolService.findSchoolById(uuid) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find school. Please try again") }
            ?: return event.replyErrorEmbed("That school does not exist ${Emoji.THINKING.getAsChat()}")

        val courses = try { courseService.findEmptyAssignmentsBySchoolInGuild(school) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find courses. Please try again") }

        if (courses.isEmpty()) return event.replyErrorEmbed("There are no courses in this school with assignments! ${Emoji.STOP_SIGN.getAsChat()}")

        val courseMenu = SelectMenu("ASSIGNMENT_REMOVAL_${event.user.idLong}_${event.slashEvent.id}") {
            courses.forEachIndexed {  index, course -> option(course.name, index.toString()) }
        }

        val menuEvent = event.awaitMenu(menu = courseMenu, message = "Select a course to remove an assignment from", deleteAfter = true) ?: return
        val index = menuEvent.values.first().toInt()
        val course = courses[index]


        val assignments = try { assignmentService.findByCourse(course) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find assignments. Please try again") }
        if (assignments.isEmpty()) return event.replyErrorEmbed("This shouldn't have happened but... There are no assignments in this course! ${Emoji.STOP_SIGN.getAsChat()}")

        val assignmentMenu = SelectMenu("ASSIGNMENT_REMOVAL_${event.user.idLong}_${event.slashEvent.id}") {
            assignments.forEachIndexed {  index, assignment -> option(assignment.name, index.toString()) }
        }

        val assignmentEvent = event.awaitMenu(menu = assignmentMenu, message = "Select an assignment to remove", deleteAfter = true, acknowledge = true) ?: return
        val assignmentIndex = assignmentEvent.values.first().toInt()
        val assignment = assignments[assignmentIndex]


        assignmentEvent.send(content = "Are you sure you want to remove the assignment `${assignment.name}` from the course `${course.name}`? ${Emoji.THINKING.getAsChat()}", actionRows = getActionRows(event, assignment, assignmentService))


    }

    private fun getActionRows(event: CommandEvent, assignment: Assignment, assignmentService: AssignmentService): List<ActionRow>
    {
        val jda = event.jda
        val yes = jda.button(user = event.user, style = ButtonStyle.PRIMARY, expiration = 1.minutes, label = "Yes") {
            it.message.editMessage("Deleting assignment `${assignment.name}` from the course `${assignment.course.name}`..)").setActionRows(emptyList()).queue()

            try
            {
                assignmentService.delete(assignment)
            }
            catch (e: Exception)
            {
                return@button it.editMessage(" ${Emoji.STOP_SIGN.getAsChat()} Error has occurred while trying to delete assignments").queue()
            }

            it.message.editMessage("Assignment ${assignment.name} deleted from the course ${assignment.course.name}").queue()
        }

        val no = jda.button(user = event.user, style = ButtonStyle.DANGER, expiration = 1.minutes, label = "No" ) {
            it.message.editMessage("Cancelling removal of assignment ${assignment.name} from the course ${assignment.course.name}..)").setActionRows(emptyList()).queue()
        }

        return listOf(yes, no).into()
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)

    {
        val guildId = event.guild?.idLong ?: return logger.error("Guild is null. This should never happen.")
        val schools = try { schoolService.findByNonEmptyCoursesInGuild(guildId) } catch (e: Exception) { return  }


        event.replyChoiceAndLimit(
            schools.map { Command.Choice(it.name, it.id.toString()) },
        ).queue()
    }
}