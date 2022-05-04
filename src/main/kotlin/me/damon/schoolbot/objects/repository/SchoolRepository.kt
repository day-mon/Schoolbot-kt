package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SchoolRepository : JpaRepository<School, UUID>
{

    fun querySchoolsByGuildId(guildId: Long): List<School>
    fun findSchoolByNameIgnoreCaseAndGuildId(name: String, guildId: Long): School?
    fun findSchoolsByIsPittSchoolAndGuildId(isPittSchool: Boolean = true, guildId: Long): List<School>
    fun findByProfessorIsNotEmptyAndGuildIdEquals(guildId: Long): List<School>
    fun findByClassesIsEmptyAndGuildId(guildId: Long): List<School>


    @Query("select s from School s where s.guildId = ?1")
    fun findInGuild(guildId: Long): MutableSet<School>

}
