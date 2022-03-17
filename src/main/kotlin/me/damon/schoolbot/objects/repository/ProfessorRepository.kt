package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProfessorRepository : JpaRepository<Professor, UUID>
{
    fun findByFullNameEqualsIgnoreCaseAndSchool(fullName: String, school: School): Professor?
    fun findProfessorBySchool(school: School): Professor?
}