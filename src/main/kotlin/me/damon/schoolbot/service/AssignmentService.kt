package me.damon.schoolbot.service

import me.damon.schoolbot.ext.logger
import me.damon.schoolbot.objects.repository.AssignmentRepository
import me.damon.schoolbot.objects.school.Assignment
import org.springframework.stereotype.Service

@Service("AssignmentService")
class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
) : SpringService
{
    fun save(assignment: Assignment) = runCatching { assignmentRepository.save(assignment) }
        .onFailure { logger.error("Error has occurred while trying to save an assignment") }
        .getOrThrow()
}