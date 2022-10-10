package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.CourseReminder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

interface CourseReminderRepository : JpaRepository<CourseReminder, UUID>
{
    @Modifying
    @Transactional
    @Query("delete from CourseReminder c where c.course = ?1")
    fun deleteAllByCourse(course: Course)

    @Async
    fun findAllByCourse(course: Course): CompletableFuture<List<CourseReminder>>

    @Query("select * from course_reminders where remind_time < now() ", nativeQuery = true)
    fun findAllExpired(): List<CourseReminder>


    @Query("select count(c) from CourseReminder c where c.course = ?1")
    fun countByCourse(course: Course): Long

    // DELETE FROM course_reminders AS cr WHERE cr.remind_time >= '2022-11-23 00:00:00' AND cr.remind_time < '2022-11-23 23:59:59' AND cr.course = '1e6e849f-c6c2-3291-92ac-c7b765fe4f78';
    @Query("delete from CourseReminder c where c.remindTime >= ?2 and c.remindTime < ?3 and c.course = ?1")
    @Transactional
    @Modifying
    fun deleteRemindersByDate(course: Course, start: Instant, end: Instant): Int



}
