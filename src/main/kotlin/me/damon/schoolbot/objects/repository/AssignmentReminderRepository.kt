package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.AssignmentReminder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.CompletableFuture

interface AssignmentReminderRepository : JpaRepository<AssignmentReminder, UUID>
{

    @Query("select a from AssignmentReminder a where a.assignment = ?1")
    @Async
    fun findByAssignment(assignment: Assignment): CompletableFuture<List<AssignmentReminder>>

    @Query("select a from AssignmentReminder a where a.assignment = ?1")
    fun findByAssignmentBlock(assignment: Assignment): List<AssignmentReminder>


    @Modifying
    @Transactional
    @Query("delete from AssignmentReminder a where a.assignment = ?1")
    fun deleteAllByAssignment(assignment: Assignment)

    @Query("select * from assignment_reminders where remind_time < now() ", nativeQuery = true)
    fun findByExpiredAssignments(): List<AssignmentReminder>

}