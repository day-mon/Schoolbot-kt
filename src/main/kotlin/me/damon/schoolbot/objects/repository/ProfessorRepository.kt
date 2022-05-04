package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ProfessorRepository : JpaRepository<Professor, UUID>
{
    @Query("select p from Professor p where upper(p.fullName) = upper(?1) and p.school = ?2")
    fun findProfessorByName(fullName: String, school: School): Professor?
    @Query("select p from Professor p where p.school = ?1")
    fun findAllBySchool(school: School): MutableSet<Professor>
    @Query("select p from Professor p where p.school.guildId = ?1")
    fun findAllInGuild(guildId: Long): MutableSet<Professor>
    @Query("select p from Professor p where p.school.name = ?1 and p.school.guildId = ?2")
    fun findAllBySchoolNameInGuild(name: String, guildId: Long): MutableSet<Professor>
    @Query("select p from Professor p where upper(p.fullName) = upper(?1) and p.school.guildId = ?2")
    fun findByNameInGuild(fullName: String, guildId: Long): Optional<Professor>

    fun findAllBySchoolId(schoolId: UUID): List<Professor>
}
