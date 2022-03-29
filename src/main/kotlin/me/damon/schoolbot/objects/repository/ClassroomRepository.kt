package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ClassroomRepository : JpaRepository<Course, UUID>
{
    @Query("select c from courses c where c.name = ?1 and c.number = ?2 and c.termIdentifier = ?3")
    fun findCourseByNameAndNumberAndTermIdentifier(name: String, number: Long, termIdentifier: String): Course?

    @Query("select c from courses c where c.guildId = ?1")
    fun findByGuildIdEquals(guildId: Long): Set<Course>

    fun findBySchool(school: School): Set<Course>

}