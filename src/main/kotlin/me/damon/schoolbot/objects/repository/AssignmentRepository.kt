package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Assignment
import me.damon.schoolbot.objects.school.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.scheduling.annotation.Async
import java.util.*
import java.util.concurrent.CompletableFuture

interface AssignmentRepository : JpaRepository<Assignment, UUID>
{
    @Query("select a from assignment a where a.course = ?1")
    @Async
    fun findByCourse(course: Course): CompletableFuture<List<Assignment>>
}
