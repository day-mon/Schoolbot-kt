package me.damon.schoolbot.service
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.School
import org.springframework.stereotype.Service
import java.util.*
import kotlin.random.Random

@Service("SchoolService")
class SchoolService(
    private val schoolRepository: SchoolRepository,
    private val professorService: ProfessorService
)
{
    private val logger by SLF4J

    suspend fun saveSchool(school: School, commandEvent: CommandEvent): School
    {
        val guild = commandEvent.guild
        val role = guild.createRole().setColor(Random.nextInt(0xFFFFF)).setName(school.name.replace(Constants.SPACE_REGEX, "-")).await()

        school.apply {
            roleId = role.idLong
            guildId = guild.idLong
        }

        return runCatching { schoolRepository.save(school) }.onFailure {
            logger.error("Error occurred while trying to save the school", it)
            role.delete().queue()
        }.getOrThrow()
    }
    
    fun update(school: School): School = runCatching { schoolRepository.save(school) }
        .onFailure { logger.error("Error occurred while trying to save the school", it) }
        .getOrThrow()

    suspend fun findSchoolsWithNoClasses(guildId: Long): List<School> =
        runCatching { schoolRepository.findEmptyClassesInGuild(guildId).await() }
            .onFailure { logger.error("Error occurred while retrieving schools with no classrooms in guild {}", guildId) }
            .getOrThrow()


    suspend fun findSchoolsInGuild(guildId: Long): List<School> =
        runCatching { schoolRepository.findInGuild(guildId).await() }
            .onFailure { logger.error("Error occurred while retrieving schools in guild {}", guildId) }
            .getOrThrow()

    suspend fun findNonEmptySchoolsInGuild(guildId: Long): List<School> =
        runCatching { schoolRepository.findByNonEmptyInAndGuildId(guildId).await() }
            .onFailure { logger.error("Error has occurred while trying to find non-empty classes.", it) }
            .getOrThrow()



    suspend fun findByEmptyProfessors(guildId: Long): List<School> =
        runCatching { schoolRepository.findByEmptyProfessorsInGuild(guildId).await() }
            .onFailure { logger.error("Error occurred while trying to grab the schools for guild {}", guildId, it) }
            .getOrThrow()

    suspend fun getPittSchoolsInGuild(guildId: Long) =
        runCatching { schoolRepository.findByPittSchoolInGuild(guildId = guildId).await() }
            .onFailure { logger.error("Error has occurred while trying to get schools by id") }
            .getOrThrow()

    suspend fun findSchoolInGuild(guildId: Long, name: String): School? =
        runCatching { schoolRepository.findByNameInGuild(name, guildId).await() }
            .onFailure { logger.error("Error occurred while trying to fetch {} in guild {}", name, guildId, it) }
            .getOrThrow()

    fun findSchoolById(id: UUID): School? = runCatching { schoolRepository.findById(id).orElse(null) }
        .onFailure { logger.error("Error occurred while trying to fetch school by id [{}]", id, it) }
        .getOrThrow()


    fun deleteSchool(school: School, event: CommandEvent)
    {
        professorService.deleteBySchool(school)
        schoolRepository.delete(school)

        event.jda.getRoleById(school.roleId)?.delete()?.queue(null) {
            logger.error("An error has occurred while trying to delete role for {}", school.name, it)
        } ?: return
    }


    suspend fun findByNonEmptyCoursesInGuild(guildId: Long): List<School> =
        runCatching { schoolRepository.findByNonEmptyCoursesInGuild(guildId).await() }
            .onFailure { logger.error("Error has occurred while trying to find non-empty classes.", it) }
            .onSuccess { logger.debug("{}", it.size) }
            .getOrThrow()



}