package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProfessorRepository : JpaRepository<Professor, UUID>
{
    fun findByFullNameEqualsIgnoreCaseAndSchool(fullName: String, school: School): Optional<Professor>
    fun findProfessorBySchool(school: School): Set<Professor>
    fun findBySchool_GuildId(guildId: Long): Set<Professor>
    fun findBySchool_NameAndSchool_GuildId(name: String, guildId: Long): Set<Professor>
    fun findByFullNameEqualsIgnoreCaseAndSchool_GuildIdEquals(fullName: String, guildId: Long): Optional<Professor>
}
