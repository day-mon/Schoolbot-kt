package me.damon.schoolbot.commands.sub.school.course

import de.jollyday.HolidayCalendar
import de.jollyday.HolidayManager
import de.jollyday.ManagerParameters
import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.*
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.service.AssignmentReminderService
import me.damon.schoolbot.service.CourseReminderService
import me.damon.schoolbot.service.CourseService
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.stereotype.Component
import java.awt.Color
import java.time.Instant
import java.time.LocalDate


@Component
class CourseCancel(
    private val courseReminderService: CourseReminderService,
    private val courseService: CourseService
) : SubCommand(
    name = "cancel",
    category = CommandCategory.SCHOOL,
    description = "Allows you to cancel a course given a day",
    options = listOf(
        CommandOptionData(
            name = "course_name",
            optionType = OptionType.STRING,
            autoCompleteEnabled = true,
            isRequired = true,
            description = "Name of the course you want to cancel | OR | all if you want do do all courses"
        ),
        CommandOptionData<String>(
            name = "date",
            optionType = OptionType.STRING,
            autoCompleteEnabled = true,
            description = "The given date you want to cancel the course on (Will imply today if nothing) Format: MM/dd/yyyy"
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val courseIdPot = event.getOption<String>("course_name")
        val date = event.getOption<String>("date")
        val cancelDate =
            if (date.isEmpty()) LocalDate.now()
        else
            try { LocalDate.parse(date, Constants.DEFAULT_DATE_FORMAT) }
            catch (e: Exception) { return event.replyErrorEmbed("Error parsing date. Try MM/dd/yyyy") }

        if (courseIdPot.lowercase() == "all")
        {
            val courses = courseService.findAllByGuild(event.guildId)
            val resultMap = mutableMapOf<Course, DatabaseCourseReturn>()


            for (it in courses)
            {
                val zone = it.school.zone

                val offset = zone.rules.getOffset(cancelDate.atStartOfDay())

                val instant = cancelDate.atStartOfDay().toInstant(offset)

                if (it.endDate.isBefore(instant))
                {
                    resultMap[it] = DatabaseCourseReturn(0, false, "Course has already ended")
                    continue
                }

                try { val ret = courseReminderService.cancelReminderOnDate(it, instant); resultMap[it] = DatabaseCourseReturn(ret, true) }
                catch (e: Exception) { resultMap[it] = DatabaseCourseReturn(0, false) }
            }

            val pages = courseCancelEmbeds(resultMap, cancelDate)
            event.sendPaginator(*pages.toTypedArray())
            return;

        }


        val courseId = courseIdPot.toUUID()
            ?: return event.replyErrorEmbed("Error while parsing course id")

        val course = courseService.findById(courseId)
            ?: return event.replyErrorEmbed("Error occurred while trying to find course")


        val zone = course.school.zone

        val offset = zone.rules.getOffset(cancelDate.atStartOfDay())

        val instant = cancelDate.atStartOfDay().toInstant(offset)

        if (course.endDate.isBefore(instant))
            return event.replyErrorEmbed("Course has already ended by ${instant.toDiscordTimeZone()}")


     val rows = try { courseReminderService.cancelReminderOnDate(course, instant) }
       catch (e: Exception) { return event.replyErrorEmbed("Error occurred while trying to cancel course") }

        if (rows == 0) {
            event.replyMessage("Successfully removed $rows reminders for the course `${course.name.toTitleCase()}`on `${cancelDate.format(Constants.DEFAULT_DATE_FORMAT)}`")
        } else {
            event.replyMessage("There were no reminders to remove")
        }
    }

    private fun courseCancelEmbeds(resultMap: MutableMap<Course, DatabaseCourseReturn>, date: LocalDate): List<MessageEmbed>
    {
       return resultMap.map { (k, v) ->
            if (!v.wasSuccessful)
            {
                Embed {
                    title = "Error has occurred"
                    description = "Something went wrong while trying to cancel ${k.name.toTitleCase()} on ${date.format(Constants.DEFAULT_DATE_FORMAT)} "
                    color = Constants.RED
                }
            }
            else
            {
                val modifiedText = if (v.rowsModified == 0) "No reminders were removed ${(v.error ?: "")}" else "Removed ${v.rowsModified} reminders"
                Embed {
                    title = "Successfully canceled ${k.name.toTitleCase()}"
                    field { name = "Courses Canceled"; value = modifiedText }
                    description = "I will not remind you about ${k.name.toTitleCase()} on ${date.format(Constants.DEFAULT_DATE_FORMAT)}"
                    color = 0x00FF00
                }
            }
        }

    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)
    {
        val option = event.focusedOption.name

        if (option == "course_name")
        {
            val courses = courseService.findAllByGuild(event.guild!!.idLong)
            event.replyChoiceAndLimit(
                courses.map { Command.Choice(it.name, it.id.toString()) }
            ).queue()
        }
        else if (option == "date")
        {
            val todayDate = LocalDate.now();
            val m = Constants.AMERICAN_HOLIDAYS
            val holidays = m.getHolidays(todayDate, LocalDate.of(todayDate.year, 12, 31))
            val dates = holidays.map { Command.Choice("${it.description} - ${it.date.format(Constants.DEFAULT_DATE_FORMAT)}", it.date.format(Constants.DEFAULT_DATE_FORMAT)) }
            event.replyChoiceAndLimit(dates).queue()

        }
    }

    data class DatabaseCourseReturn(val rowsModified: Int, val wasSuccessful: Boolean, val error: String? = null)
}