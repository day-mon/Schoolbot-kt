package me.damon.schoolbot.service

import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.ext.toDiscordTimeZone
import me.damon.schoolbot.objects.repository.CourseReminderRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.CourseReminder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service("CourseReminderService")
class CourseReminderService(
    private val courseReminderRepository: CourseReminderRepository,
)
{
    val logger by SLF4J

    fun saveAll(courseReminders: Iterable<CourseReminder>): MutableList<CourseReminder>
    {
        val now = Instant.now()
        val filteredReminders = courseReminders.filter { it.remindTime.isAfter(now) }

        return runCatching { courseReminderRepository.saveAll(filteredReminders) }
            .onFailure { logger.error("Error has occurred while trying to save reminders for a course") }.getOrThrow()
    }

    fun cancelReminderOnDate(course: Course, instant: Instant): Int
     {
        // convert instant to localdate
         val localDate = LocalDate.ofInstant(instant, course.school.zone)
         val startOfDay = LocalDateTime.of(localDate, LocalTime.of(0, 0))
         val endOfDay = LocalDateTime.of(localDate, LocalTime.of(23, 59))

         val startOfDayInstant =  startOfDay.toInstant(course.school.zone.rules.getOffset(startOfDay))
         val endOfDayInstant =  endOfDay.toInstant(course.school.zone.rules.getOffset(endOfDay))


         logger.info("{}", startOfDayInstant.toDiscordTimeZone())
         logger.info("{}", endOfDayInstant.toDiscordTimeZone())


         return runCatching { courseReminderRepository.deleteRemindersByDate(course, startOfDayInstant, endOfDayInstant) }
            .onFailure { logger.error("Error has occurred while trying to cancel reminders for a course", it) }
            .getOrThrow()
    }



    fun deleteAllByCourse(course: Course) = runCatching { courseReminderRepository.deleteAllByCourse(course) }
        .onFailure { logger.error("Error has occurred while trying to delete reminders for a course") }
        .getOrThrow()

     fun findAllExpiredReminders() = runCatching { courseReminderRepository.findAllExpired()  }
        .onFailure { logger.error("Error has occurred while trying to get reminders") }
        .getOrThrow()

    fun remove(courseReminder: CourseReminder) = runCatching { courseReminderRepository.delete(courseReminder) }
        .onFailure { logger.error("Error has occurred while trying to remove a reminder") }
        .getOrThrow()

    fun findCountByCourse(course: Course) = runCatching { courseReminderRepository.countByCourse(course) }
        .onFailure { logger.error("Error has occurred while trying to count reminders by course") }
        .getOrThrow()








}