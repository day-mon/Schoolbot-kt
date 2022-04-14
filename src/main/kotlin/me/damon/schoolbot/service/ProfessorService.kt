package me.damon.schoolbot.service

import dev.minn.jda.ktx.SLF4J
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


    open fun getProfessorsInGuild(guildId: Long): Optional<Set<Professor>> =
        runCatching { Optional.of(professorRepository.findBySchool_GuildId(guildId)) }
            .onFailure { logger.error("Error has occurred while trying to get professors in guild $guildId") }
            .getOrDefault(Optional.empty())

    open fun saveProfessor(professor: Professor): Optional<Professor> = runCatching { Optional.of(professorRepository.save(professor)) }
        .onFailure { logger.error("Error occurred while trying to save professor", it) }
        .getOrDefault(Optional.empty())


     open fun saveProfessors(professors: Collection<Professor>): Result<Iterable<Professor>> =
        runCatching { professorRepository.saveAll(professors) }

    open fun removeProfessors(professors: Collection<Professor>): Result<Unit> =
        runCatching { professorRepository.deleteAll(professors) }



    open fun findProfessorsBySchool(school: School): Set<Professor> =
        runCatching { professorRepository.findProfessorBySchool(school) }
            .onFailure { logger.error("Error occurred while retrieving professors in school {}", school.name) }
            .getOrThrow()

    open fun findProfessorsBySchool(name: String, guildId: Long): Set<Professor>?  =
        runCatching { professorRepository.findBySchool_NameAndSchool_GuildId(name, guildId) }
            .onFailure { logger.error("Error has occurred while trying to find professors in guild $guildId") }
            .getOrNull()

    open fun findProfessorById(id: UUID): Optional<Professor> = runCatching { professorRepository.findById(id) }
        .onFailure { logger.error("Error occurred while retrieving professor with id {}", id) }
        .getOrDefault(Optional.empty())

    open fun findProfessorsByGuild(guildId: Long): Set<Professor> =
        runCatching { professorRepository.findBySchool_GuildId(guildId) }
            .onFailure { logger.error("Error occurred while retrieving professors in guild {}", guildId) }
            .getOrThrow()

    open fun findProfessorByName(name: String, school: School): Optional<Professor> =
        runCatching { professorRepository.findByFullNameEqualsIgnoreCaseAndSchool(name, school) }
            .onFailure { logger.error("Error occurred while trying to get professor", it) }
            .getOrDefault(Optional.empty())

}