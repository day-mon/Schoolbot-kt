package me.damon.schoolbot.service

import dev.minn.jda.ktx.SLF4J
import me.damon.schoolbot.objects.repository.CourseReminderRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.CourseReminder
import org.springframework.stereotype.Service

@Service("CourseReminderService")
open class CourseReminderService(
    private val courseReminderRepository: CourseReminderRepository
) : SpringService
{
    val logger by SLF4J

    open fun saveAll(courseReminders: Iterable<CourseReminder>): MutableList<CourseReminder> = runCatching { courseReminderRepository.saveAll(courseReminders) }
        .onFailure { logger.error("Error has occurred while trying to save reminders for a course") }
        .getOrThrow()

    open fun deleteAllByCourse(course: Course) = runCatching { courseReminderRepository.deleteAllByCourse(course) }
        .onFailure { logger.error("Error has occurred while trying to delete reminders for a course") }
        .getOrThrow()



}