package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.scheduling.annotation.Async
import java.util.*
import java.util.concurrent.CompletableFuture

interface ClassroomRepository : JpaRepository<Course, UUID>
{
    @Query("select c from courses c where c.number = ?1 and c.termIdentifier = ?2")
    @Async
    fun findByNumberAndIdentifier(number: Long, termIdentifier: String): CompletableFuture<Course?>

    @Query("select c from courses c where c.guildId = ?1")
    @Async
    fun findAllByGuild(guildId: Long): CompletableFuture<List<Course>>

    @Query("select c from courses c where c.school = ?1")
    @Async
    fun findBySchool(school: School): CompletableFuture<List<Course>>

    @Query("select c from courses c where c.assignments is empty and c.guildId = ?1")
    @Async
    fun findAllByEmptyAssignmentsInGuild(guildId: Long):  CompletableFuture<List<Course>>

    @Query("select c from courses c where c.assignments is not empty and c.school = ?1")
    @Async
    fun findByNonEmptyAssignmentsInSchool(school: School): CompletableFuture<List<Course>>


}