package me.damon.schoolbot.objects.repository;

import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.AssignmentReminder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface AssignmentReminderRepository : JpaRepository<AssignmentReminder, UUID>
{
    @Modifying
    @Transactional
    @Query("delete from AssignmentReminder a where a.assignment = ?1")
    fun deleteAllByAssignment(assignment: Assignment)
}