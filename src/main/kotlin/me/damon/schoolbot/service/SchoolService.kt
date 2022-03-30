package me.damon.schoolbot.service

import dev.minn.jda.ktx.SLF4J
import dev.minn.jda.ktx.await
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.models.CourseModel
import me.damon.schoolbot.objects.repository.ClassroomRepository
import me.damon.schoolbot.objects.repository.ProfessorRepository
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@Service("SchoolService")
open class SchoolService(
    private val professorRepository: ProfessorRepository,
    private val schoolRepository: SchoolRepository,
    private val classroomRepository: ClassroomRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
)
{

    private val logger by SLF4J
    private val regex = Regex("\\s+")
    private val random = ThreadLocalRandom.current()


    // @Cacheable
    open suspend fun saveSchool(school: School, commandEvent: CommandEvent): School?
    {
        val guild = commandEvent.guild
       // val role = guild.createRole().setColor(random.nextInt(0xFFFFF)).setName(school.name.replace(regex, "-")).await()



        school.apply {
           // roleId = 0L
            guildId = guild.idLong
        }

        return runCatching { schoolRepository.save(school) }.onFailure {
            logger.error("Error occurred while trying to save the school", it)
          //  role.delete().queue()
        }.getOrNull()

    }

     private fun <T: Identifiable> getRepository(identifiable: T): JpaRepository<in Any, UUID> = when (identifiable) {
        is Professor -> professorRepository
        is School -> schoolRepository
        is Course -> classroomRepository
       // is Assignment -> AssignmentRepository
        // is GuildSettings -> GuildSettingsRepository
        else -> throw IllegalArgumentException("Unknown identifiable type")
    }


      private inline fun <reified T : Identifiable> updateEntity(identifiable: T): T?
      {
          val repository = getRepository(identifiable)
          val opt = repository.findByIdOrNull(identifiable.id)  ?: return run {
              logger.warn("Could not find identifiable with id ${identifiable.id}")
              null
          }


          return runCatching { repository.save(identifiable) }
              .onFailure { logger.error("Error occurred while trying to save the identifiable", it) }
              .getOrNull()
      }


    // write a function that creates the Declaration of Independence
    /*
    open fun updateEntity(entity: Identifiable): Identifiable? = when (entity)
    {


        is School ->
        {
            val schoolOpt = schoolRepository.findById(entity.id)

            if (schoolOpt.isEmpty) run {
                logger.error("School passed through does not exist in the database.")
                null
            }

            val school = schoolOpt.get()

            runCatching { schoolRepository.save(school) }
                .onFailure { logger.error("Error has occurred while trying update ${school.name}") }
                .onSuccess { logger.info("Successfully updated ${school.name}") }
                .getOrNull()
        }

        is Professor ->
        {
            val professorOpt = professorRepository.findById(entity.id)

            if (professorOpt.isEmpty) run {
                logger.error("School passed through does not exist in the database.")
                null
            }

            val professor = professorOpt.get()

            runCatching { professorRepository.save(professor) }
                .onFailure { logger.error("Error has occurred while trying update ${school.name}") }
                .onSuccess { logger.info("Successfully updated ${school.name}") }
                .getOrNull()
        }

        is Course ->
        {
            val courseOpt = classroomRepository.findById(entity.id)

            if (courseOpt.isEmpty) run {
                logger.error("Course passed through does not exist in the database.")
                null
            }

            val course = courseOpt.get()

            runCatching { classroomRepository.save(course) }
                .onFailure { logger.error("Error has occurred while trying to update ${course.name}") }
                .onSuccess { logger.info("Successfully updated ${course.name}") }
                .getOrNull()
        }
        else -> throw NotImplementedError("${entity.javaClass.simpleName} has not been implemented")
    }

     */



    open fun getProfessorsInGuild(guildId: Long): Set<Professor>? =
        runCatching { professorRepository.findBySchool_GuildId(guildId) }
            .onFailure { logger.error("Error has occurred while trying to get professors in guild $guildId") }
            .getOrNull()

    open fun saveProfessor(professor: Professor): Professor? = runCatching { professorRepository.save(professor) }
        .onFailure { logger.error("Error occurred while trying to save professor", it) }.getOrNull()


    open fun saveProfessors(professors: Collection<Professor>): Result<Iterable<Professor>> =
        runCatching { professorRepository.saveAll(professors) }

    open fun removeProfessors(professors: Collection<Professor>): Result<Unit> =
        runCatching { professorRepository.deleteAll(professors) }


    open fun findProfessorsBySchool(name: String, guildId: Long): Set<Professor>? =
        runCatching { professorRepository.findBySchool_NameAndSchool_GuildId(name, guildId) }
            .onFailure { logger.error("Error has occurred while trying to find professors in guild $guildId") }
            .getOrNull()

    open fun findProfessorsBySchool(school: School): Set<Professor>? =
        runCatching { professorRepository.findProfessorBySchool(school) }
            .onFailure { logger.error("Error occurred while retrieving professors in school {}", school.name) }
            .getOrNull()
   /*
    open fun findProfessorsInCourse(course: Course): Professor? =
        runCatching { professorRepository.findProfessorByCourse(course) }
            .onFailure { logger.error("Error occurred while retrieving professors in course {}", course.name) }
            .getOrNull()

    */


    open fun findProfessorById(id: UUID): Professor? = runCatching { professorRepository.findById(id).orElse(null) }
        .onFailure { logger.error("Error occurred while retrieving professor with id {}", id) }
        .getOrNull()

    open fun findProfessorsByGuild(guildId: Long): Set<Professor>? =
        runCatching { professorRepository.findBySchool_GuildId(guildId) }
            .onFailure { logger.error("Error occurred while retrieving professors in guild {}", guildId) }
            .getOrNull()

    open suspend fun createPittCourse(
        commandEvent: CommandEvent, school: School, courseModel: CourseModel
    ): Course?
    {

        val guild = commandEvent.guild

        val course = courseModel.asCourse(
            school = school,
            professorRepository

        )

        val professors = findProfessorsBySchool(school) ?: return run {
            logger.error("Error occurred while retrieving professors in school {}", school.name)
            null
        }



       val professorDif = course.professors.filter { it !in professors }

        if (professorDif.isNotEmpty())
        {
            saveProfessors(professorDif).onFailure {
                logger.error("Professors cannot be saved", it)
                return null
            }
        }

        val role = guild.createRole().setColor(random.nextInt(0xFFFFF))
            .setName(courseModel.name.replace(regex, "-").lowercase()).await()

        val channel = guild.createTextChannel(courseModel.name.replace(regex, "-").lowercase())
            .addPermissionOverride(role, Permission.ALL_CHANNEL_PERMISSIONS, 0L)
            .addPermissionOverride(guild.publicRole, 0L, Permission.ALL_CHANNEL_PERMISSIONS).await()

        course.apply {
            channelId = channel.idLong
            roleId = role.idLong
        }

        return runCatching { classroomRepository.save(course) }.onFailure {
            logger.error("Error has occurred during the save", it)
            runCleanUp(course, commandEvent, professorDif)
        }.getOrNull()


    }

    open suspend fun deleteCourse(course: Course, commandEvent: CommandEvent): Unit?
    {
        return runCatching { classroomRepository.delete(course) }
            .onFailure { logger.error("Error has occurred while trying to delete {} ", course.name, it) }
            .onSuccess { runCleanUp(course, commandEvent) }.getOrNull()
    }


    open fun findCoursesByGuild(guildId: Long): Set<Course>? =
        runCatching { classroomRepository.findByGuildIdEquals(guildId) }.onFailure {
                logger.error(
                    "Error has occurred while trying to get the courses for guild id: {}",
                    guildId,
                    it
                )
            }.getOrNull()

    open fun findProfessorByName(name: String, school: School): Professor? =
        runCatching { professorRepository.findByFullNameEqualsIgnoreCaseAndSchool(name, school) }
            .onFailure { logger.error("Error occurred while trying to get professor", it) }.getOrNull()

    private suspend fun runCleanUp(
        course: Course, event: CommandEvent, professors: Collection<Professor> = mutableSetOf()
    )
    {
        withContext(dispatcher) {
            val guild = event.guild

            guild.getRoleById(course.roleId)?.delete()?.queue({
                logger.info(
                    "{}'s with the role id {} has been deleted successfully", course.name, course.roleId
                )
            }, { logger.error("Error has occurred while attempt to delete role during clean up.", it) }) ?: logger.warn(
                "{}'s role does not exist", course.name
            )
            guild.getTextChannelById(course.channelId)?.delete()?.queue({
                logger.info(
                    "{}'s with the channel id {} has been deleted successfully", course.name, course.channelId
                )
            }, { logger.error("Error has occurred while attempt to delete channel during clean up.", it) })
                ?: logger.warn("{}'s channel does not exist", course.name)

            removeProfessors(professors = professors).onFailure {
                logger.error(
                    "Error occurred while trying to remove professors added during the add process", it
                )
            }
        }
    }



    open suspend fun findDuplicateCourse(name: String, number: Long, termId: String) = withContext(dispatcher) {
        classroomRepository.findCourseByNameAndNumberAndTermIdentifier(name, number, termId)
    }


    open fun getClassesInGuild(guildId: Long): Set<Course>? =
        runCatching { classroomRepository.findByGuildIdEquals(guildId) }
            .onFailure { logger.error("Error has occurred while trying to get the courses for guild id: {}", guildId, it) }.
            getOrNull()

    open fun findEmptyClassesInGuild(guildId: Long): List<Course>? =
        runCatching { classroomRepository.findByAssignmentsIsEmptyAndGuildIdEquals(guildId) }
            .onFailure { logger.error("Error has o") }
            .getOrNull()

    open fun getClassesBySchool(school: School): Set<Course>? =
        runCatching { classroomRepository.findBySchool(school = school) }
            .onFailure { logger.error("Error has occurred while trying to get the courses for school id: {}", school.name, it) }.
            getOrNull()


    open fun getSchoolsByGuildId(guildId: Long): List<School>? =
        runCatching { schoolRepository.querySchoolsByGuildId(guildId) }
            .onFailure { logger.error("Error occurred while trying to grab the schools for guild {}", guildId, it) }
            .onSuccess { logger.debug("Schools returned for {} - {}", guildId, it) }.getOrNull()

    open fun getSchoolsWithProfessorsInGuild(guildId: Long): List<School>? =
        runCatching { schoolRepository.findByProfessorIsNotEmptyAndGuildIdEquals(guildId) }
            .onFailure { logger.error("Error occurred while trying to grab the schools for guild {}", guildId, it) }
            .getOrNull()

    open fun getPittSchoolsInGuild(guildId: Long) =
        runCatching { schoolRepository.findSchoolsByIsPittSchoolAndGuildId(guildId = guildId) }
            .onFailure { logger.error("Error has occurred while trying to get schools by id") }.getOrNull()

    open fun findSchoolInGuild(guildId: Long, name: String): School? =
        runCatching { schoolRepository.findSchoolByNameIgnoreCaseAndGuildId(name, guildId) }
            .onFailure { logger.error("Error occurred while trying to fetch {} in guild {}", name, guildId, it) }
            .getOrNull()

    open fun findSchoolById(id: UUID): Optional<School>? = runCatching { schoolRepository.findById(id) }
        .onFailure { logger.error("Error occurred while trying to fetch school by id [{}]", id, it) }
        .getOrNull()


    open fun findDuplicateSchool(guildId: Long, name: String): Boolean? =
        runCatching { schoolRepository.findSchoolByNameIgnoreCaseAndGuildId(name, guildId) }
            .onFailure { logger.error("Error occurred while trying to fetch {} in guild {}", name, guildId, it) }
            .getOrNull() == null

    // @CacheEvict(cacheNames = ["schoolsByGuild"], key = "#school.guildId")
    open fun deleteSchool(school: School, event: CommandEvent)
    {
        schoolRepository.delete(school)
        event.jda.getRoleById(school.roleId)?.delete()?.queue(null) {
            logger.error("An error has occurred while trying to delete role for {}", school.name, it)
        } ?: return
    }


}