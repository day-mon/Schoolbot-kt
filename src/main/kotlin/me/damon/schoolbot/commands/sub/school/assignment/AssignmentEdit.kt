package me.damon.schoolbot.commands.sub.school.assignment

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option
import me.damon.schoolbot.Constants
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.service.AssignmentService
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
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

        val assignmentAttributeMenu = SelectMenu("ASSIGNMENT_ATTRIBUTE_EDIT_${event.user.idLong}_${event.slashEvent.id}") {
            option("Name", "name")
            option("Description", "description")
            option("Due Date", "dueDate")
            option("Due time", "dueTime")
            option("Points", "points")
        }
        val assignmentAttributeEvent = event.awaitMenu(menu = assignmentAttributeMenu, message = "Select an attribute to edit", throwAway = true) ?: return
        val assignmentAttribute = assignmentAttributeEvent.values.first()

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