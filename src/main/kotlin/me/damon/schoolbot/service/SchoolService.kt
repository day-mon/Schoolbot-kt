package me.damon.schoolbot.service

import dev.minn.jda.ktx.SLF4J
import dev.minn.jda.ktx.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.models.CourseModel
import me.damon.schoolbot.objects.repository.ClassroomRepository
import me.damon.schoolbot.objects.repository.ProfessorRepository
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.Permission
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.*

@Service("SchoolService")
open class SchoolService(
    private val professorRepository: ProfessorRepository,
    private val schoolRepository: SchoolRepository,
    private val classroomRepository: ClassroomRepository
)
{

    private val logger by SLF4J
    private val regex = Regex("\\s+")
    private val random = Random()

    // @Cacheable
    open suspend fun saveSchool(school: School, commandEvent: CommandEvent): Result<School>
    {
        val guild = commandEvent.guild
        /*
        val role = guild.createRole()
            .setColor(random.nextInt(0xFFFFF))
            .setName(school.name.replace(regex, "-"))
            .await()

         */

        school.apply {
            roleId = 0L
            guildId = guild.idLong
        }

       return runCatching {
           schoolRepository.save(school)
       }
    }

    open fun saveProfessor(professor: Professor): Professor? = runCatching { professorRepository.save(professor) }.onFailure {
        logger.error("Error occurred while trying to save professor", it)
    }.getOrNull()

    open fun getProfessorsInSchool(school: School): Professor? = runCatching { professorRepository.findProfessorBySchool(school) }.onFailure {
        logger.error("Error occurred while retrieving professors in school {}", school.name)
    }.getOrNull()

    open fun saveProfessors(professors: Collection<Professor>): Result<Iterable<Professor>> = runCatching { professorRepository.saveAll(professors) }

    open fun removeProfessors(professors: Collection<Professor>): Result<Unit> = runCatching { professorRepository.deleteAll(professors) }

    open suspend fun createPittCourse(commandEvent: CommandEvent, school: School, courseModel: CourseModel): Result<Course>
    {
        val guild = commandEvent.guild

        val course = courseModel.asCourse(
            school = school,
        )

        val professorDif = course.professors.filter { it !in course.school.professor }

        if (professorDif.isNotEmpty())
        {
            saveProfessors(professorDif).onFailure {
                logger.error("Professors cannot be saved", it)
                return Result.failure(it)
            }
        }

        val role = guild.createRole()
            .setColor(random.nextInt(0xFFFFF))
            .setName(courseModel.name.replace(regex, "-"))
            .await()

        val channel = guild.createTextChannel(courseModel.name.replace(regex, "-"))
            .addPermissionOverride(role, Permission.ALL_CHANNEL_PERMISSIONS, 0L)
            .addPermissionOverride(guild.publicRole, 0L, Permission.ALL_CHANNEL_PERMISSIONS)
            .await()

        course.apply {
            channelId = channel.idLong
            roleId = role.idLong
        }

        return runCatching { classroomRepository.save(course) }.onFailure {
            logger.error("Error has occurred during the save", it)
                runCleanUp(course, commandEvent, professorDif)

        }
    }

    open suspend fun deleteCourse(course: Course, commandEvent: CommandEvent): Unit?
    {
        return runCatching { classroomRepository.delete(course) }.onFailure {
            logger.error("Error has occurred while trying to delete {} ", course.name, it)
        }.onSuccess { runCleanUp(course, commandEvent)  }.getOrNull()
    }


    open fun findCoursesByGuild(guildId: Long): Result<Set<Course>>
    =
    runCatching { classroomRepository.findByGuildIdEquals(guildId) }
        .onFailure { logger.error("Error has occurred while trying to get the courses for guild id: {}", guildId, it) }

    open fun findProfessorByName(name: String, school: School): Professor? = runCatching { professorRepository.findByFullNameEqualsIgnoreCaseAndSchool(name, school) }.onFailure {
        logger.error("Error occurred while trying to get professor", it)
    }.getOrNull()

    private suspend fun runCleanUp(course: Course, event: CommandEvent, professors: Collection<Professor> = course.professors)
    {
        withContext(Dispatchers.IO) {
            val guild = event.guild

            guild.getRoleById(course.roleId)?.delete()?.queue({
                logger.info(
                    "{}'s with the role id {} has been deleted successfully",
                    course.name,
                    course.roleId
                )
            },
                { logger.error("Error has occurred while attempt to delete role during clean up.", it) })
                ?: logger.warn("{}'s role does not exist", course.name)
            guild.getTextChannelById(course.channelId)?.delete()?.queue({
                logger.info(
                    "{}'s with the channel id {} has been deleted successfully",
                    course.name,
                    course.channelId
                )
            }, { logger.error("Error has occurred while attempt to delete channel during clean up.", it) })
                ?: logger.warn("{}'s channel does not exist", course.name)

            removeProfessors(professors = professors).onFailure {
                logger.error("Error occurred while trying to remove professors added during the add process", it)
            }
        }
    }


    open suspend fun findDuplicateCourse(name: String, number: Long, termId: String) =
        withContext(Dispatchers.IO) {
            classroomRepository.findCourseByNameAndNumberAndTermIdentifier(name, number, termId)
        }


    @Cacheable(cacheNames = ["guildSchools"], key = "#guildId")
    open fun getSchoolsByGuildId(guildId: Long): List<School>? = runCatching {   schoolRepository.querySchoolsByGuildId(guildId) }.onFailure {
        logger.error("Error occurred while trying to grab the schools for guild {}", it)
    }.getOrNull()

    open fun getPittSchoolsInGuild(guildId: Long) = schoolRepository.findSchoolsByIsPittSchoolAndGuildId(guildId = guildId)

    open fun findSchoolInGuild(guildId: Long, name: String) = schoolRepository.findSchoolByNameAndGuildId(name, guildId)

   // @CacheEvict(cacheNames = ["schoolsByGuild"], key = "#school.guildId")
    open fun deleteSchool(school: School, event: CommandEvent)
    {
        schoolRepository.delete(school)
        event.jda.getRoleById(school.roleId)?.delete()?.queue( null) {
            logger.error("An error has occurred while trying to delete role for {}", school.name, it)
        } ?: return
    }







}