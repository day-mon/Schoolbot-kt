package me.damon.schoolbot.commands.sub.school.assignment

import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.editMessage_
import dev.minn.jda.ktx.messages.into
import me.damon.schoolbot.Constants
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.getValue
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.ext.send
import me.damon.schoolbot.ext.toOffset
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.AssignmentService
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.time.*
import java.util.*

class AssignmentEdit : SubCommand(
    name = "edit",
    description = "Edit an assignment",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "school",
            description = "The school to edit the assignment in",
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
        val uuid = try { UUID.fromString(uuidStr) } catch (e: Exception) { return event.replyErrorEmbed("That school does not have any assignments available to edit at this time ") }
        val school = try { event.getService<SchoolService>().findSchoolById(uuid) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find school. Please try again") }
            ?: return event.replyErrorEmbed("That school does not exist ${Emoji.THINKING.getAsChat()}")

        val courses = try { event.getService<CourseService>().findEmptyAssignmentsBySchoolInGuild(school) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find courses. Please try again") }

        if (courses.isEmpty()) return event.replyErrorEmbed("There are no courses in this school with assignments! ${Emoji.STOP_SIGN.getAsChat()}")

        val courseMenu = SelectMenu("ASSIGNMENT_EDIT_${event.user.idLong}_${event.slashEvent.id}") {
            courses.forEachIndexed {  index, course -> option(course.name, index.toString()) }
        }

        val menuEvent = event.awaitMenu(menu = courseMenu, message = "Select a course to edit an assignment from", throwAway = true) ?: return
        val index = menuEvent.values.first().toInt()
        val course = courses[index]

        val assignmentService = event.getService<AssignmentService>()

        val assignments = try { assignmentService.findByCourse(course) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find assignments. Please try again") }
        if (assignments.isEmpty()) return event.replyErrorEmbed("This shouldn't have happened but... There are no assignments in this course! ${Emoji.STOP_SIGN.getAsChat()}")



        val assignmentMenu = SelectMenu("ASSIGNMENT_EDIT_${event.user.idLong}_${event.slashEvent.id}") {
            assignments.forEachIndexed { index, assignment -> option(assignment.name, index.toString()) }
        }
        val assignmentEvent = event.awaitMenu(menu = assignmentMenu, message = "Select an assignment to edit", throwAway = true) ?: return
        val assignmentIndex = assignmentEvent.values.first().toInt()
        val assignment = assignments[assignmentIndex]
        val dateTime = assignment.dueDate.atZone(school.zone)

        val modal = Modal("assignment_edit", "Edit Assignment") {
            short(label = "Assignment Name", value = assignment.name, id = "name")
            paragraph(label = "Assignment Description", value = assignment.description, id = "description")
            short(label = "Assignment Due Date", value = dateTime.toLocalDate().format(Constants.DEFAULT_DATE_FORMAT), id = "dueDate")
            short(label = "Due Date Time", value = dateTime.toLocalTime().format(Constants.DEFAULT_TIME_FORMAT), id = "dueTime")
            short(label = "Assignment Points", value = assignment.points.toString(), id = "points")
        }

        val modalEvent = event.awaitModal(interaction = assignmentEvent, modal = modal, deferReply = true) ?: return
        val name = modalEvent.getValue<String>("name") ?: return event.replyErrorEmbed("You must enter a name for the assignment. It cannot be empty")
        val description = modalEvent.getValue<String>("description") ?: return event.replyErrorEmbed("You must enter a description for the assignment. It cannot be empty")
        val dueDate = modalEvent.getValue<LocalDate>("dueDate") ?: return event.replyErrorEmbed("Please enter a valid due date.\nFormat: MM/DD/YYYY")
        val dueTime = modalEvent.getValue<LocalTime>("dueTime") ?:  return event.replyErrorEmbed("Please enter a valid due time.\nFormat: HH:MM AM/PM")
        val points = modalEvent.getValue<Int>("points") ?: return event.replyErrorEmbed("You must enter a number for the points")

        val updateReminders = dueDate.isEqual(dateTime.toLocalDate()) // kinda dumb but whatever
        assignment.apply {
            this.name = name
            this.description = description
            this.dueDate = LocalDateTime.of(dueDate, dueTime).toInstant(school.zone.toOffset())
            this.points = points
        }

        modalEvent.send(content = "This is the new assignment information. Are you sure you want to edit it?", embed = assignment.getAsEmbed(), actionRows = getActionRows(event, updateReminders, assignment, school))

    }

    private fun getActionRows(event: CommandEvent, updateReminders: Boolean, assignment: Assignment, school: School): List<ActionRow>
    {
        val jda = event.jda
        val yesButton = jda.button(style = ButtonStyle.PRIMARY, label = "Yes") {
            it.message.edit("Editing assignment...", components = emptyList()).queue()
            val service = event.getService<AssignmentService>()

            try { if (updateReminders) service.update(assignment) else service.save(assignment) }
            catch (e: Exception) { return@button it.message.edit(content = "Error has occurred while trying to update as `${assignment.name}`").queue() }

            it.message.edit(content = "Assignment updated successfully!", embed = assignment.getAsEmbed()).queue()
        }

        val noButton = jda.button(style = ButtonStyle.DANGER, label = "No") {
            it.message.edit("Cancelling update of ${assignment.name}", components = emptyList()).queue()
        }

        return listOf(yesButton, noButton).into()
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val guildId = event.guild?.idLong ?: return logger.error("Guild is null. This should never happen.")
        val schools = try { schoolbot.schoolService.findByNonEmptyCoursesInGuild(guildId) } catch (e: Exception) { return  }


        event.replyChoiceAndLimit(
            schools.map { Command.Choice(it.name, it.id.toString()) },
        ).queue()
    }
}