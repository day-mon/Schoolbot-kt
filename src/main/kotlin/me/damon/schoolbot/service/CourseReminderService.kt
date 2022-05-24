package me.damon.schoolbot.service

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.objects.repository.CourseReminderRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.CourseReminder
import org.springframework.stereotype.Service
import java.time.Instant

@Service("CourseReminderService")
class CourseReminderService(
    private val courseReminderRepository: CourseReminderRepository,
) : SpringService
{
    val logger by SLF4J

    fun saveAll(courseReminders: Iterable<CourseReminder>): MutableList<CourseReminder>
    {
        val now = Instant.now()
        val filteredReminders = courseReminders.filter { it.remindTime.isAfter(now) }

        return runCatching { courseReminderRepository.saveAll(filteredReminders) }
            .onFailure { logger.error("Error has occurred while trying to save reminders for a course") }.getOrThrow()
    }

    fun deleteAllByCourse(course: Course) = runCatching { courseReminderRepository.deleteAllByCourse(course) }
        .onFailure { logger.error("Error has occurred while trying to delete reminders for a course") }
        .getOrThrow()

    suspend fun findByCourse(course: Course) = runCatching { courseReminderRepository.findAllByCourse(course).await() }
        .onFailure { logger.error("Error has occurred while trying to get reminders for {}", course.name) }
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