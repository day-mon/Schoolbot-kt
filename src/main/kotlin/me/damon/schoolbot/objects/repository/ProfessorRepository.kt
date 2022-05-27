package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.CompletableFuture

interface ProfessorRepository : JpaRepository<Professor, UUID>
{

    @Transactional
    @Modifying
    @Query("delete from Professor p where p.school = ?1")
    fun deleteAllInGuild(guildId: Long)

    @Async
    @Query("select p from Professor p where upper(p.fullName) = upper(?1) and p.school = ?2")
    fun findProfessorByName(fullName: String, school: School): CompletableFuture<Professor?>
    @Async
    @Query("select p from Professor p where p.school = ?1")
    fun findAllBySchool(school: School): CompletableFuture<List<Professor>>

    @Async
    @Query("select p from Professor p where p.school.guildId = ?1")
    fun findAllInGuild(guildId: Long): CompletableFuture<List<Professor>>

    @Async
    @Query("select p from Professor p where p.school.name = ?1 and p.school.guildId = ?2")
    fun findAllBySchoolNameInGuild(name: String, guildId: Long): CompletableFuture<List<Professor>>
    @Query("select p from Professor p where upper(p.fullName) = upper(?1) and p.school.guildId = ?2")
    fun findByNameInGuild(fullName: String, guildId: Long): Optional<Professor>

    fun findAllBySchoolId(schoolId: UUID): List<Professor>

    @Modifying
    @Transactional
    @Query("delete from Professor p where p.school = ?1")
    fun deleteBySchool(school: School)

}
