package me.damon.schoolbot.commands.sub.school.assignment

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.ext.toUUID
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
import org.springframework.stereotype.Component

@Component
class AssignmentView(
    private val schoolService: SchoolService,
    private val courseService: CourseService,
    private val assignmentService: AssignmentService
) : SubCommand(
    name = "view",
    description = "View an assignment",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "school",
            description = "The school to view the assignment from",
            isRequired = true,
            optionType = OptionType.STRING,
            autoCompleteEnabled = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val uuid = event.getOption<String>("school").toUUID()
            ?: return event.replyErrorEmbed("That school does not have any assignments to view at this time")
        val school = try { schoolService.findSchoolById(uuid) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find school. Please try again") }
            ?: return event.replyErrorEmbed("That school does not exist ${Emoji.THINKING.getAsChat()}")

        val courses = try { courseService.findEmptyAssignmentsBySchoolInGuild(school) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find courses. Please try again") }

        if (courses.isEmpty()) return event.replyErrorEmbed("There are no courses in this school with assignments! ${Emoji.STOP_SIGN.getAsChat()}")

        val courseMenu = SelectMenu("ASSIGNMENT_VIEW_${event.user.idLong}_${event.slashEvent.id}") {
            courses.forEachIndexed { index, course -> option(course.name, index.toString()) }
        }

        val menuEvent = event.awaitMenu(menu = courseMenu, message = "Select a course to view an assignment from", deleteAfter = true) ?: return
        val index = menuEvent.values.first().toInt()
        val course = courses[index]


        val assignments = try { assignmentService.findByCourse(course) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find assignments. Please try again") }
        if (assignments.isEmpty()) return event.replyErrorEmbed("This shouldn't have happened but... There are no assignments in this course! ${Emoji.STOP_SIGN.getAsChat()}")

        event.sendPaginator(assignments)
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