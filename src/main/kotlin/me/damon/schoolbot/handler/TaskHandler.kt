package me.damon.schoolbot.handler

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.ext.toDiscordTimeZone
import me.damon.schoolbot.objects.school.CourseReminder
import me.damon.schoolbot.service.CourseReminderService
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.GuildService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory
import java.time.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
class TaskHandler(
    private val courseReminderService: CourseReminderService,
    private val schoolService: SchoolService,
    private val guildService: GuildService,
    private val courseService: CourseService
)
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
        timeUnit: TimeUnit,
        duration: Long,
        block: () -> Unit
    ): ScheduledFuture<*>
    {
        val job = scheduler.scheduleAtFixedRate(
            /* command = */ block,
            /* initialDelay = */ delay,
            /* period = */ duration,
            /* unit = */ timeUnit
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
            timeUnit = TimeUnit.MINUTES,
            duration = 5
        ) {
            jda.presence.setPresence(OnlineStatus.ONLINE, activityList.random())
        }

        addRepeatingTask(
            name = "course_reminders",
            timeUnit = TimeUnit.SECONDS,
            duration = 10
        ) {
           // val reminders = courseReminderService.findAllExpiredReminders()
           //  reminders.forEach { sendCourseAlert(it, jda) }

        }


        addRepeatingTask(
            name = "assignment_reminders",
            timeUnit = TimeUnit.SECONDS,
            duration = 10,
            block = {

            })
    }

    fun cancelTask(name: String) = tasks[name]?.cancel(false)

    private fun sendCourseAlert(reminder: CourseReminder, jda: JDA)
    {
        val course = reminder.course
        val school = course.school
        val due = Duration.between(Instant.now(), reminder.remindTime).toMinutes()
        val channel = jda.getTextChannelById(course.channelId) ?: return logger.error("{} has no channel", course.name )
        val mention = jda.getRoleById(course.roleId)?.asMention ?: "Students of ${course.name}"
        val courseName = course.name // make this title case if you get around to it



        val jvmUptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().uptime).toSeconds()


        // could run into some issues on bot start up with the bot spamming
        val dueMessage = if (due <= 0 ) "$mention **$courseName** is **now starting**"
                         else "$mention, **$courseName** is starting in **${due + 1} minutes**"


        // courseReminderService.remove(reminder)

        val availableReminders = courseReminderService.findCountByCourse(course)
        val deleteAfterLastReminder = guildService.getGuildSettings(channel.guild.idLong).deleteRemindableEntityOnLastReminder

        val zone = ZoneId.of(school.timeZone)
        val lt = LocalTime.ofInstant(reminder.remindTime, zone)
        val ld = LocalDate.now(zone)
        val instant = LocalDateTime.of(ld, lt).toInstant(zone.rules.getOffset(Instant.now()))

        val sendingEmbed = Embed {
            title = "Reminder for $courseName"
            logger.info("{}", course.url)
            if (course.url.isNotBlank()) url = course.url
            field(name = "Time until class start", value = dueMessage, inline = false)
            field(name = "Class start time today", value = instant.toDiscordTimeZone(), inline = false)
            if (reminder.specialMessage.isNotBlank()) field(name = "Special Message", value = reminder.specialMessage, inline = false)
        }

        channel.sendMessageEmbeds(sendingEmbed).queue()

        if (availableReminders == 0L && deleteAfterLastReminder)
        {
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


        logger.debug("Due time: {} min", due)

    }
}