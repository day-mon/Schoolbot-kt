package me.damon.schoolbot.service

import dev.minn.jda.ktx.coroutines.await
import me.damon.schoolbot.ext.logger
import me.damon.schoolbot.objects.repository.AssignmentReminderRepository
import me.damon.schoolbot.objects.repository.AssignmentRepository
import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.AssignmentReminder
import org.springframework.stereotype.Service
import java.time.Instant

@Service("AssignmentReminderService")
class AssignmentReminderService(
    val assignmentRepository: AssignmentRepository,
    private val assignmentReminderRepository: AssignmentReminderRepository
) : SpringService
{
    fun saveAll(reminders: List<AssignmentReminder>): MutableList<AssignmentReminder>
    {
        val now = Instant.now()
        val filteredReminders = reminders.filter { it.remindTime.isAfter(now) }

        return runCatching { assignmentReminderRepository.saveAll(filteredReminders) }
            .onFailure { logger.error("Error has occurred while attempting to save reminders") }
            .getOrThrow()
    }


    fun deleteByAssignment(assignment: Assignment) = runCatching { assignmentReminderRepository.deleteAllByAssignment(assignment) }
        .onFailure { logger.error("Error has occurred while attempting to delete reminders") }
        .getOrThrow()

    suspend fun findByAssignment(assignment: Assignment) = runCatching { assignmentReminderRepository.findByAssignment(assignment).await() }
        .onFailure { logger.error("Error has occurred while attempting to find reminders") }
        .getOrThrow()
}