package me.damon.schoolbot.handler

import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.ext.toDiscordTimeZone
import me.damon.schoolbot.ext.toDiscordTimeZoneRelative
import me.damon.schoolbot.ext.toTitleCase
import me.damon.schoolbot.objects.school.AssignmentReminder
import me.damon.schoolbot.objects.school.CourseReminder
import me.damon.schoolbot.service.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import org.springframework.stereotype.Component
import java.time.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Component
class TaskHandler(
    private val courseReminderService: CourseReminderService,
    private val assignmentReminderService: AssignmentReminderService,
    private val guildService: GuildService,
    private val courseService: CourseService,
    private val assignmentService: AssignmentService
) : CoroutineEventListener
{
    private val activityList = listOf(
        Activity.watching("mark sleep"),
        Activity.streaming("warner growing", "https://www.youtube.com/watch?v=PLOPygVcaVE"),
        Activity.watching("damon bench joesphs weight"),
        Activity.streaming("chakra balancing seminar", "https://www.youtube.com/watch?v=vqklftk89Nw")
    )
    private val scheduler = Executors.newScheduledThreadPool(10) { Thread(it, "Schoolbot TaskHandler-Thread") }
    private val logger by SLF4J
    val tasks = mutableMapOf<String, Future<*>>()



    fun addRepeatingTask(
        name: String,
        delay: Long = 0,
        duration: kotlin.time.Duration,
        block: () -> Unit
    ): ScheduledFuture<*>
    {
        val job = scheduler.scheduleAtFixedRate(
            /* p0 = */ block,
            /* p1 = */ delay,
            /* p2 = */ duration.inWholeMilliseconds,
            /* p3 = */ TimeUnit.MILLISECONDS
        )
        tasks[name] = job
        return job
    }

    fun addTask(name: String, timeUnit: TimeUnit, duration: Long, block: () -> Unit)
    {
        val job = scheduler.schedule(
            block, duration, timeUnit
        )
        tasks[name] = job
        logger.info("Task with ID [{}] has been scheduled for {} {}(s)", name, duration, timeUnit.name.lowercase())

    }

    fun taskExist(name: String) = tasks.containsKey(name)


    fun startOnReadyTask(jda: JDA)
    {
        addRepeatingTask(
            name = "status_switcher",
            duration = 5.minutes
        ) {
            jda.presence.setPresence(OnlineStatus.ONLINE, activityList.random())
        }

        addRepeatingTask(
            name = "course_reminders",
            duration = 10.seconds
        ) {
            try
            {
                val reminders = courseReminderService.findAllExpiredReminders()
                reminders.forEach { sendCourseAlert(it, jda) }
            }
            catch (e: Exception)
            {
                logger.error("Error has occurred while sending reminders.")
            }
        }


        addRepeatingTask(
            name = "assignment_reminders",
            duration = 10.seconds
        ) {
            try
            {
                val reminders = assignmentReminderService.findAllExpiredReminders()
                reminders.forEach { sendAssignmentAlert(it, jda) }
            }
            catch (e: Exception)
            {
                logger.error("Error has occurred in sending assignment alert", e)
            }
        }
        logger.info("Started {} tasks named -> {}", tasks.size, tasks.keys.joinToString { it })
    }

    fun cancelTask(name: String) = tasks[name]?.cancel(false)


    private fun sendAssignmentAlert(reminder: AssignmentReminder, jda: JDA)
    {
        val course = reminder.assignment.course
        val assignment = reminder.assignment

        val assignmentName = when {
            reminder.assignment.name.length > MessageEmbed.TITLE_MAX_LENGTH -> "${
                reminder.assignment.name.substring(
                    0,
                    MessageEmbed.TITLE_MAX_LENGTH - 5
                )
            }..."
            else -> reminder.assignment.name
        }

        val channel = jda.getTextChannelById(course.channelId) ?: return logger.warn("Channel with ID [{}] not found", course.channelId)
        val mention = jda.getRoleById(course.roleId)?.asMention ?: return logger.warn("Role with ID [{}] not found", course.roleId)
        val dueMessage = when {
            assignment.dueDate.isBefore(Instant.now().minusSeconds(300)) -> "$mention, Sorry I could not remind you on time. $assignmentName was due ${assignment.dueDate.toDiscordTimeZoneRelative()}"
            else -> "$mention, **$assignmentName** is due ${assignment.dueDate.toDiscordTimeZoneRelative()}"
        }
        // 10/19/2022 03:59 PM
        // 10/19/2022 7:19 PM




        val sendingEmbed = Embed {
            this.title = "Reminder for $assignmentName"
            field(name = "Description", value = assignment.description, inline = false)
            field(name = "Points", value = assignment.points.toString(), inline = false)
            field(name = "Time until assignment is due", value = dueMessage, inline = true)
            field(name = "Due Date", value = reminder.assignment.dueDate.toDiscordTimeZone(), inline = true)
            field(inline = true)
            if (reminder.message.isNotBlank()) field(name = "Message", value = reminder.message, inline = false)
        }


        channel.sendMessageEmbeds(sendingEmbed).queue()

        assignmentReminderService.delete(reminder)

        val deleteAfterLastReminder = guildService.getGuildSettings(channel.guild.idLong).deleteRemindableEntityOnLastReminder

        val assignmentReminders = assignmentReminderService.findByAssignmentBlock(assignment)

        if (assignmentReminders.isEmpty() && deleteAfterLastReminder)
        {
            try { assignmentService.delete(assignment) }
            catch (e: Exception)
            {
                channel.sendMessage("An error occurred while trying to delete ${course.name}.").queue(null) {
                    ErrorHandler().handle(ErrorResponse.UNKNOWN_CHANNEL) {
                        logger.error("Could not warn user about class not being able to be deleted.. Idk what to do here to be quite honest")
                    }
                }
            }
        }
    }

    private fun sendCourseAlert(reminder: CourseReminder, jda: JDA)
    {
        val course = reminder.course
        val channel = jda.getTextChannelById(course.channelId) ?: return logger.warn("Channel with ID [{}] not found", course.channelId)
        val mention = jda.getRoleById(course.roleId)?.asMention ?: "Students of ${course.name}"

        val courseName = when {
            course.name.length > MessageEmbed.TITLE_MAX_LENGTH -> "${
                course.name.substring(
                    0,
                    MessageEmbed.TITLE_MAX_LENGTH - 5
                )
            }..."
            else -> course.name
        }


        val localTime = reminder.remindTime.atZone(course.school.zone).toLocalTime()

        val instant = localTime.atDate(LocalDate.now()).atZone(course.school.zone).toInstant()

        val dueMessage = when {
            instant.isBefore(Instant.now().minusSeconds(300)) -> "$mention, Sorry I could not remind you on time. $courseName was supposed to start ${instant.toDiscordTimeZoneRelative()}"
            else -> "$mention, **$courseName** is starting in ${instant.toDiscordTimeZoneRelative()}"
        }

        val sendingEmbed = Embed {
            title = "Reminder for $courseName"
            if (course.url.isNotBlank()) url = course.url
            field(name = "Time until class start", value = dueMessage, inline = false)
            field(name = "Class start time today", value = instant.toDiscordTimeZone(), inline = false)
            if (reminder.specialMessage.isNotBlank()) field(name = "Special Message", value = reminder.specialMessage, inline = false)
        }

        channel.sendMessageEmbeds(sendingEmbed).queue()

        courseReminderService.remove(reminder)

        // get count of reminders left
        val remindersLeft = courseReminderService.findCountByCourse(course)

        val deleteAfterLastReminder = guildService.getGuildSettings(channel.guild.idLong).deleteRemindableEntityOnLastReminder

        if (remindersLeft == 0L && deleteAfterLastReminder)
        {
            val assignments = assignmentService.findByCourseBlock(course)
            if (assignments.isNotEmpty())
            {
                return channel.sendMessage("There are still assignments for ${course.name} that need to be completed. Please delete them before deleting the course.").queue(null) {
                    ErrorHandler().handle(ErrorResponse.UNKNOWN_CHANNEL) {
                        logger.error("Could not warn user about class not being able to be deleted.. Idk what to do here to be quite honest")
                    }
                }
            }


            try { courseService.deleteCourse(course, jda) }
            catch (e: Exception)
            {
                channel.sendMessage("An error occurred while trying to delete ${course.name}.").queue(null) {
                    ErrorHandler().handle(ErrorResponse.UNKNOWN_CHANNEL) {
                        logger.error("Could not warn user about class not being able to be deleted.. Idk what to do here to be quite honest")
                    }
                }
            }
        }
    }

    override suspend fun onEvent(event: GenericEvent)
    {
        when (event) {
            is ReadyEvent -> startOnReadyTask(event.jda)
        }
    }
}
