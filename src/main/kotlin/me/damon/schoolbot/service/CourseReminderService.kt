package me.damon.schoolbot.service

import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.objects.repository.CourseReminderRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.CourseReminder
import org.springframework.stereotype.Service

@Service("CourseReminderService")
class CourseReminderService(
    private val courseReminderRepository: CourseReminderRepository
) : SpringService
{
    val logger by SLF4J

    fun saveAll(courseReminders: Iterable<CourseReminder>): MutableList<CourseReminder> = runCatching { courseReminderRepository.saveAll(courseReminders) }
        .onFailure { logger.error("Error has occurred while trying to save reminders for a course") }
        .getOrThrow()

    fun deleteAllByCourse(course: Course) = runCatching { courseReminderRepository.deleteAllByCourse(course) }
        .onFailure { logger.error("Error has occurred while trying to delete reminders for a course") }
        .getOrThrow()



}