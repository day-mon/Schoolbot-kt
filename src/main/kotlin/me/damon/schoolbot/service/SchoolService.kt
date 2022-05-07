package me.damon.schoolbot.service
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.School
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@Service("SchoolService")
open class SchoolService(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SpringService
{


    @Autowired
    lateinit var schoolRepository: SchoolRepository

    private val logger by SLF4J
    private val regex = Regex("\\s+")
    private val random = ThreadLocalRandom.current()


    // @Cacheable
    open suspend fun saveSchool(school: School, commandEvent: CommandEvent): School
    {
        val guild = commandEvent.guild
        val role = guild.createRole().setColor(random.nextInt(0xFFFFF)).setName(school.name.replace(regex, "-")).await()



        school.apply {
            roleId = role.idLong
            guildId = guild.idLong
        }

        return runCatching { schoolRepository.save(school) }.onFailure {
            logger.error("Error occurred while trying to save the school", it)
            role.delete().queue()
        }.getOrThrow()
    }
    
    open fun update(school: School): School = runCatching { schoolRepository.save(school) }.onFailure {
        logger.error("Error occurred while trying to save the school", it)
    }.getOrThrow()

    open fun findSchoolsWithNoClasses(guildId: Long):List<School> =
        runCatching { schoolRepository.findEmptyClassesInGuild(guildId) }
            .onFailure { logger.error("Error occurred while retrieving schools with no classrooms in guild {}", guildId) }
            .getOrThrow()


    open fun findSchoolsInGuild(guildId: Long): List<School> =
        runCatching { schoolRepository.findInGuild(guildId) }
            .onFailure { logger.error("Error occurred while retrieving schools in guild {}", guildId) }
            .getOrThrow()



    open fun findByEmptyProfessors(guildId: Long): List<School> =
        runCatching { schoolRepository.findByEmptyProfessorsInGuild(guildId) }
            .onFailure { logger.error("Error occurred while trying to grab the schools for guild {}", guildId, it) }
            .getOrThrow()

    open fun getPittSchoolsInGuild(guildId: Long) =
        runCatching { schoolRepository.findByPittSchoolInGuild(guildId = guildId) }
            .onFailure { logger.error("Error has occurred while trying to get schools by id") }
            .getOrThrow()

    open fun findSchoolInGuild(guildId: Long, name: String): School? =
        runCatching { schoolRepository.findByNameInGuild(name, guildId) }
            .onFailure { logger.error("Error occurred while trying to fetch {} in guild {}", name, guildId, it) }
            .getOrThrow()

    open fun findSchoolById(id: UUID): School? = runCatching { schoolRepository.getById(id) }
        .onFailure { logger.error("Error occurred while trying to fetch school by id [{}]", id, it) }
        .getOrThrow()


    open fun findDuplicateSchool(guildId: Long, name: String): Boolean =
        runCatching { schoolRepository.findByNameInGuild(name, guildId) == null }
            .onFailure { logger.error("Error occurred while trying to fetch {} in guild {}", name, guildId, it) }
            .getOrThrow()

    // @CacheEvict(cacheNames = ["schoolsByGuild"], key = "#school.guildId")
    open fun deleteSchool(school: School, event: CommandEvent)
    {
        schoolRepository.delete(school)
        event.jda.getRoleById(school.roleId)?.delete()?.queue(null) {
            logger.error("An error has occurred while trying to delete role for {}", school.name, it)
        } ?: return
    }


}