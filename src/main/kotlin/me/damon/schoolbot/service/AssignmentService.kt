package me.damon.schoolbot.service

import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.runBlocking
import me.damon.schoolbot.ext.logger
import me.damon.schoolbot.objects.repository.AssignmentRepository
import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.defaultReminders
import org.springframework.stereotype.Service

@Service("AssignmentService")
class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
    private val assignmentReminderService: AssignmentReminderService
)
{
    fun save(assignment: Assignment) = runCatching { assignmentRepository.save(assignment) }
        .onFailure { logger.error("Error has occurred while trying to save an assignment") }
        .getOrThrow()

    suspend fun findByCourse(course: Course) = runCatching { assignmentRepository.findByCourse(course).await() }
        .onFailure { logger.error("Error has occurred while trying to find assignments by course") }
        .getOrThrow()

    fun findByCourseBlock(course: Course) = runCatching { assignmentRepository.findByCourseBlock(course) }
        .onFailure { logger.error("Error has occurred while trying to find assignments by course") }
        .getOrThrow()


    // load all assignments and reminders in a course
    suspend fun loadAssignmentsAndReminders(course: Course): List<Assignment>
    {
        val assignments = findByCourse(course)
        assignments.forEach { assignment ->
            assignment.reminders = assignmentReminderService.findByAssignment(assignment)
        }
        return assignments
    }

    fun delete(assignment: Assignment) = runCatching { assignmentReminderService.deleteByAssignment(assignment); assignmentRepository.delete(assignment) }
        .onFailure { logger.error("Error has occurred while trying to delete an assignment") }
        .getOrThrow()

    suspend fun update(assignment: Assignment): Assignment =
        runCatching { assignmentReminderService.deleteByAssignment(assignment);  assignmentReminderService.saveAll(assignment.getInitialReminders()); assignmentRepository.save(assignment); }
        .onFailure { logger.error("Error has occurred while trying to update an assignment") }
        .getOrThrow()
}