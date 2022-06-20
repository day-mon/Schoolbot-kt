package me.damon.schoolbot.service

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.objects.repository.ProfessorRepository
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import org.springframework.stereotype.Service
import java.util.*

@Service("ProfessorService")
 class ProfessorService(
    private val professorRepository: ProfessorRepository
)
{
    private val logger by SLF4J

    @Throws(Exception::class)
     suspend fun findAllInGuild(guildId: Long): List<Professor> =
        runCatching { professorRepository.findAllInGuild(guildId).await() }
            .onFailure { logger.error("Error has occurred while trying to get professors in guild $guildId") }
            .getOrThrow()
    @Throws
     fun save(professor: Professor): Professor = runCatching { professorRepository.save(professor) }
        .onFailure { logger.error("Error occurred while trying to save professor", it) }
        .getOrThrow()

    @Throws
     fun saveAll(professors: Collection<Professor>): Iterable<Professor> = professorRepository.saveAll(professors)

     fun removeAll(professors: Collection<Professor>) = professorRepository.deleteAll(professors)


    fun remove(professor: Professor) = runCatching { professorRepository.delete(professor) }
        .onFailure { logger.error("Error occurred while trying to remove professor", it) }
        .getOrThrow()

    fun deleteBySchool(school: School) = runCatching { professorRepository.deleteBySchool(school) }
        .onFailure { logger.error("Error has occurred while deleting professors") }
        .getOrThrow()

    @Throws
     suspend fun findBySchool(school: School): List<Professor> =
        runCatching { professorRepository.findAllBySchool(school).await() }
            .onFailure { logger.error("Error occurred while retrieving professors in school {}", school.name) }
            .getOrThrow()

     fun findBySchoolId(id: UUID): List<Professor> = runCatching { professorRepository.findAllBySchoolId(id) }
        .onFailure { logger.error("Error occurred while retrieving professors in school {}", id) }
        .getOrThrow()


    @Throws
     fun findById(id: UUID): Professor? = runCatching { professorRepository.getById(id) }
        .onFailure { logger.error("Error occurred while retrieving professor with id {}", id) }
        .getOrThrow()

    @Throws
     suspend fun findByNameInSchool(name: String, school: School): Professor? =
        runCatching { professorRepository.findProfessorByName(name, school).await() }
            .onFailure { logger.error("Error occurred while trying to get professor", it) }
            .getOrThrow()

}