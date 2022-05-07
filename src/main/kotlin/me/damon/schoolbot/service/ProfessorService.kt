package me.damon.schoolbot.service

import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.damon.schoolbot.objects.repository.ProfessorRepository
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service("ProfessorService")
open class ProfessorService(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SpringService
{
    @Autowired
    private lateinit var professorRepository: ProfessorRepository
    private val logger by SLF4J

    @Throws(Exception::class)
    open fun findAllInGuild(guildId: Long): MutableSet<Professor> =
        runCatching { professorRepository.findAllInGuild(guildId) }
            .onFailure { logger.error("Error has occurred while trying to get professors in guild $guildId") }
            .getOrThrow()
    @Throws
    open fun save(professor: Professor): Professor = runCatching { professorRepository.save(professor) }
        .onFailure { logger.error("Error occurred while trying to save professor", it) }
        .getOrThrow()

    @Throws
    open fun saveAll(professors: Collection<Professor>): Iterable<Professor> = run { professorRepository.saveAll(professors) }

    open fun removeAll(professors: Collection<Professor>) = run { professorRepository.deleteAll(professors) }


    @Throws
    open fun findBySchool(school: School): MutableSet<Professor> =
        runCatching { professorRepository.findAllBySchool(school) }
            .onFailure { logger.error("Error occurred while retrieving professors in school {}", school.name) }
            .getOrThrow()

    open fun findBySchoolId(id: UUID): List<Professor> = runCatching { professorRepository.findAllBySchoolId(id) }
        .onFailure { logger.error("Error occurred while retrieving professors in school {}", id) }
        .getOrThrow()

    @Throws
    open fun findBySchoolName(name: String, guildId: Long): MutableSet<Professor>  =
        runCatching { professorRepository.findAllBySchoolNameInGuild(name, guildId) }
            .onFailure { logger.error("Error has occurred while trying to find professors in guild $guildId") }
            .getOrThrow()
    @Throws
    open fun findById(id: UUID): Professor? = runCatching { professorRepository.getById(id) }
        .onFailure { logger.error("Error occurred while retrieving professor with id {}", id) }
        .getOrThrow()

    @Throws
    open fun findByNameInSchool(name: String, school: School): Professor? =
        runCatching { professorRepository.findProfessorByName(name, school) }
            .onFailure { logger.error("Error occurred while trying to get professor", it) }
            .getOrThrow()

}