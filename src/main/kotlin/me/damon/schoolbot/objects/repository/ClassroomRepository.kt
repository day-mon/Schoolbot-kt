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
    @Query("select c from courses c where c.name = ?1 and c.number = ?2 and c.termIdentifier = ?3")
    @Async
    fun findByNameAndNumberAndTerm(name: String, number: Long, termIdentifier: String): CompletableFuture<Course?>

    @Query("select c from courses c where c.guildId = ?1")
    fun findAllByGuild(guildId: Long): List<Course>

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