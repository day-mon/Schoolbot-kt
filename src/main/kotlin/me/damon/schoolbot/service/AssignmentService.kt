package me.damon.schoolbot.service

import dev.minn.jda.ktx.coroutines.await
import me.damon.schoolbot.ext.logger
import me.damon.schoolbot.objects.repository.AssignmentRepository
import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.Course
import org.springframework.stereotype.Service

@Service("AssignmentService")
open class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
    private val assignmentReminderService: AssignmentReminderService
) : SpringService
{
    fun save(assignment: Assignment) = runCatching { assignmentRepository.save(assignment) }
        .onFailure { logger.error("Error has occurred while trying to save an assignment") }
        .getOrThrow()

    suspend fun findByCourse(course: Course) = runCatching { assignmentRepository.findByCourse(course).await() }
        .onFailure { logger.error("Error has occurred while trying to find assignments by course") }
        .getOrThrow()

    fun delete(assignment: Assignment) = runCatching { assignmentReminderService.deleteByAssignment(assignment); assignmentRepository.delete(assignment) }
        .onFailure { logger.error("Error has occurred while trying to delete an assignment") }
        .getOrThrow()
}