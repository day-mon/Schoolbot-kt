package me.damon.schoolbot.commands.sub.school.assignment

import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.editMessage_
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Constants

import me.damon.schoolbot.ext.*
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.AssignmentType
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.service.AssignmentReminderService
import me.damon.schoolbot.service.AssignmentService
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.springframework.stereotype.Component
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@Component
class AssignmentAdd(
    private val courseService: CourseService,
    private val schoolService: SchoolService,
    private val assignmentService: AssignmentService,
    private val assignmentReminderService: AssignmentReminderService
) : SubCommand(
    name = "add",
    description = "Adds an assignment to the course",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "school",
            optionType = OptionType.STRING,
            description = "The school that has the course that you wish to add the assignment to",
            autoCompleteEnabled = true,
            isRequired = true
        )
    )

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val schoolId = event.getOption<String>("school")


        val schoolUUID = UUID.fromString(schoolId)

        val school = try
        {
            schoolService.findSchoolById(schoolUUID)
        }
        catch (e: Exception)
        {
            return event.replyErrorEmbed("Error has occurred while trying to find the school.")
        } ?: return event.replyErrorEmbed("That school has not been found.")

        val courses = try
        {
            courseService.findBySchool(school)
        }
        catch (e: Exception)
        {
            return event.replyErrorEmbed("Error has occurred while trying to find the courses. In this school.")
        }

        // this should really never happen unless someone randomly guesses an uuid lol. better safe than sorry
        if (courses.isEmpty()) return event.replyErrorEmbed("There are no courses in this school.")



        val menu = SelectMenu(customId = "ASSIGNMENT_ADD_MENU_${event.guild.id}_${event.slashEvent.id}") {
            courses.forEachIndexed { index, course -> option(course.name, index.toString()) }
        }

        val menuEvent = event.awaitMenu(
            menu,
            deleteAfter = true,
            message =  "Please select the course that you wish to add the assignment to."
        ) ?: return
        val index = menuEvent.values.first().toInt()
        val course = courses[index]

        val assignments = try { assignmentService.findByCourse(course) } catch (e: Exception) {  return event.replyErrorEmbed("Error has occurred while trying to find the assignments.") }
        if (assignments.size == Constants.SELECTION_MENU_MAX_SIZE) return event.replyErrorEmbed("There are too many assignments in this course. Please remove some assignments before adding more.")

        val title = "Add an assigment for ${course.name}"


        val modal = Modal("assignment-add-modal", if (title.length > Modal.MAX_TITLE_LENGTH) "${title.substring(0, Modal.MAX_TITLE_LENGTH - 5)}..." else title) {
            short(id = "assignment-name", label = "Assignment Name", required = true)
            paragraph(id = "assignment-add-description", label = "Description", required = true)
            short(id = "assignment-add-due-date", placeholder = "Format MM/dd/yyyy", label = "Due Date", required = true)
            short(id = "assignment-add-due-time", placeholder = "Format hh:mm AM/PM", label = "Due Time", required = true)
            short(id = "assignment-add-points", label = "Points", required = true, requiredLength = 1..3)
        }


        val modalEvent = menuEvent.awaitModal(
            modal = modal,
            deferReply = true,
        ) ?: return

        val name = modalEvent.getValue("assignment-name")?.asString ?: return
        val description = modalEvent.getValue("assignment-add-description")?.asString ?: return
        val dueDate = modalEvent.getValue("assignment-add-due-date")?.asString ?: return
        val dueTime = modalEvent.getValue("assignment-add-due-time")?.asString ?: return
        val points = modalEvent.getValue("assignment-add-points")?.asString ?: return

        val error = evaluateModalFields(dueDate, dueTime, points, course)

        if (error != String.empty) return modalEvent.replyErrorEmbed(errorString = error).queue()

        val offset = ZoneId.of(school.timeZone).rules.getOffset(Instant.now())

        val dueDateTime = LocalDateTime.parse(
            "$dueDate $dueTime",
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Constants.DEFAULT_LOCALE)
        ).toInstant(offset)

        val assignmentTypeMenu = SelectMenu(customId = "ASSIGNMENT_ADD_REMINDER_MENU_${event.guild.id}_${event.slashEvent.id}") {
            AssignmentType.values().forEachIndexed { index, type -> option(type.name.toTitleCase(), index.toString()) }
        }

        val selectMenu = modalEvent.awaitMenu(
            assignmentTypeMenu,
            deleteAfter = true,
            message = "Please select the type of assignment that you wish to add."
        ) ?: return
        val assignmentType = AssignmentType.values()[selectMenu.values.first().toInt()]


        val assignment = withContext(Dispatchers.IO) {
            Assignment(
                name = name,
                description = description,
                dueDate = dueDateTime,
                points = points.toInt(),
                course = course,
                assignmentType = assignmentType
            )
        }

        val savedAssignment = try { assignmentService.save(assignment) }
        catch (e: IllegalArgumentException) { return event.replyErrorEmbed("This should not have happened. Please contact the developer.") }
        catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to save the assignment.") }


        modalEvent.replyEmbed(savedAssignment.getAsEmbed())


        modalEvent.send(
            content = "Would you like to add reminders for this assignment?. Reminders will occur on these dates:\n${savedAssignment.getInitialReminders().joinToString (separator = "\n", transform =  { "${it.remindTime.toDiscordTimeZoneLDST()} - (${it.remindTime.toDiscordTimeZoneRelative()}) " })}",
            actionRows = getActionRows(event, assignment)
        )
    }

    private fun getActionRows(event: CommandEvent, assignment: Assignment): List<Button>
    {
        val jda = event.jda

        val yes = jda.button(style = ButtonStyle.PRIMARY, user = event.user, label = "Yes") {
            it.message.edit("Adding reminders...", components = emptyList()).queue()


            val addedReminders = try { assignmentReminderService.saveAll(assignment.getInitialReminders()) }
            catch (e: Exception) { return@button it.message.editMessage("Error has occurred while trying to save the reminders.").queue() }

            it.message.editMessage("**${addedReminders.size}** Reminders added! Have a nice day ${Emoji.SMILEY.getAsChat()} ! ${ if (addedReminders.size < 5) "\n **WAIT** Just in case you are wondering the reason why you didnt get all of the times added is because one or more of the times added has already happened." else "" }").queue()
        }

        val no = jda.button(style = ButtonStyle.DANGER, user = event.user, label = "No") {
            it.editMessage_(components = emptyList(), content = "Okay have a nice day ${Emoji.THUMB_UP.getAsChat()}").queue()
        }

        return listOf(yes, no)
    }

    private fun evaluateModalFields(
        dueDate: String, dueTime: String, points: String, course: Course
    ): String
    {
        val school = course.school
        if (points.toIntOrNull() == null) return "Points must be a number."
        val date = try
        {
            LocalDate.parse(dueDate, Constants.DEFAULT_DATE_FORMAT)
        }
        catch (e: DateTimeParseException)
        {
            return "Due date must be in the format MM/dd/yyyy \n **Example: 01/01/1970**"
        }
        if (date.isBefore(LocalDate.now(school.zone))) return "Due date must be after today."

        val startDate = LocalDate.ofInstant(course.startDate, ZoneId.of(school.timeZone))
        val endDate = LocalDate.ofInstant(course.endDate, ZoneId.of(school.timeZone))


        if (date.isBefore(startDate) || date.isAfter(endDate)) return "Due date must be between the start and end date of the course."

        try
        {
            LocalTime.parse(dueTime, Constants.DEFAULT_TIME_FORMAT)
        }
        catch (e: DateTimeParseException)
        {
            return "Due time must be in the format hh:mm AM/PM \n ** Example: 05:00 PM**"
        }

        // repeated code from above
        val dueDateTime = LocalDateTime.parse(
            "$dueDate $dueTime",
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Constants.DEFAULT_LOCALE)
        )

        if (dueDateTime.isBefore(LocalDateTime.now(school.zone))) return "Due time must be after today."

        return String.empty
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)
    {
        val guildId = event.guild?.idLong ?: return logger.error("Guild is null. This should never happen.")
        val schools = try
        {
            schoolService.findNonEmptySchoolsInGuild(guildId)
        }
        catch (e: Exception)
        {
            return
        }
        event.replyChoiceAndLimit(schools.map { Command.Choice(it.name, it.id.toString()) }).queue()

    }
}