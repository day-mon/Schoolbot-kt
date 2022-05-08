package me.damon.schoolbot.service

import me.damon.schoolbot.ext.logger
import me.damon.schoolbot.objects.repository.AssignmentReminderRepository
import me.damon.schoolbot.objects.repository.AssignmentRepository
import me.damon.schoolbot.objects.school.AssignmentReminder
import org.springframework.stereotype.Service

@Service("AssignmentReminderService")
class AssignmentReminderService(
    val assignmentRepository: AssignmentRepository,
    val assignmentReminderRepository: AssignmentReminderRepository
) : SpringService
{
    fun saveAll(reminders: List<AssignmentReminder>) = runCatching { assignmentReminderRepository.saveAll(reminders) }
        .onFailure { logger.error("Error has occurred while attempting to save reminders") }
        .getOrThrow()
}