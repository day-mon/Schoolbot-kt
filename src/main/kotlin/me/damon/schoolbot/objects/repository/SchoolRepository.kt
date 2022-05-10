package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.CompletableFuture

@Repository
interface SchoolRepository : JpaRepository<School, UUID>
{

    @Query("select s from School s where upper(s.name) = upper(?1) and s.guildId = ?2")
    @Async
    fun findByNameInGuild(name: String, guildId: Long): CompletableFuture<School?>
    @Query("select s from School s where s.isPittSchool = ?1 and s.guildId = ?2")
    @Async
    fun findByPittSchoolInGuild(isPittSchool: Boolean = true, guildId: Long): CompletableFuture<List<School>>
    @Query("select s from School s where s.professor is not empty and s.guildId = ?1")
    @Async
    fun findByEmptyProfessorsInGuild(guildId: Long): CompletableFuture<List<School>>
    @Query("select s from School s where s.classes is empty and s.guildId = ?1")
    @Async
    fun findEmptyClassesInGuild(guildId: Long): CompletableFuture<List<School>>
    @Query("select s from School s where s.guildId = ?1")
    fun findInGuild(guildId: Long): List<School>
    @Query("select s from School s where s.classes is not empty and s.guildId = ?1")
    @Async
    fun findByNonEmptyInAndGuildId(guildId: Long): CompletableFuture<List<School>>

    @Query("select s from School s where s.classes is not empty and s.guildId = ?1")
    @Async
    fun findByNonEmptyCoursesInGuild(guildId: Long): CompletableFuture<List<School>>

}
