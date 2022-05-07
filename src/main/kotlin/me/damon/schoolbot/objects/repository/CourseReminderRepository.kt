package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.CourseReminder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface CourseReminderRepository : JpaRepository<CourseReminder, UUID>
{
    @Modifying
    @Transactional
    @Query("delete from CourseReminder c where c.course = ?1")
    fun deleteAllByCourse(course: Course)
}
