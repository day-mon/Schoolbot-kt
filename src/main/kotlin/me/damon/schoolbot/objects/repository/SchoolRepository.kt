package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SchoolRepository : JpaRepository<School, UUID>
{


    @Query("select s from School s where upper(s.name) = upper(?1) and s.guildId = ?2")
    fun findByNameInGuild(name: String, guildId: Long): School?
    @Query("select s from School s where s.isPittSchool = ?1 and s.guildId = ?2")
    fun findByPittSchoolInGuild(isPittSchool: Boolean = true, guildId: Long): List<School>
    @Query("select s from School s where s.professor is not empty and s.guildId = ?1")
    fun findByEmptyProfessorsInGuild(guildId: Long): List<School>
    @Query("select s from School s where s.classes is empty and s.guildId = ?1")
    fun findEmptyClassesInGuild(guildId: Long): List<School>
    @Query("select s from School s where s.guildId = ?1")
    fun findInGuild(guildId: Long): List<School>
    @Query("select s from School s where s.classes is not empty and s.guildId = ?1")
    fun findByNonEmptyInAndGuildId(guildId: Long): List<School>

}
