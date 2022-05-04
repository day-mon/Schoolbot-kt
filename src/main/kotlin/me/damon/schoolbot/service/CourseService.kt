package me.damon.schoolbot.service

import dev.minn.jda.ktx.SLF4J
import dev.minn.jda.ktx.await
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.models.CourseModel
import me.damon.schoolbot.objects.repository.ClassroomRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.Permission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ThreadLocalRandom

@Service("CourseService")
open class CourseService(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SpringService
{
    @Autowired
    lateinit var classroomRepository: ClassroomRepository
    @Autowired
    lateinit var professorService: ProfessorService

    private val logger by SLF4J
    private val regex = Regex("\\s+")

    open suspend fun deleteCourse(course: Course, commandEvent: CommandEvent) =
        runCatching { classroomRepository.delete(course) }
        .onFailure { logger.error("Error has occurred while trying to delete {} ", course.name, it) }
        .onSuccess { runCleanUp(course, commandEvent) }
        .getOrThrow()


    open suspend fun createPittCourse(
        commandEvent: CommandEvent, school: School, courseModel: CourseModel
    ): Course?
    {


        val guild = commandEvent.guild
        val professorService = commandEvent.getService<ProfessorService>()

        val course = courseModel.asCourse(
            school = school,
            professorService = professorService
        )

        val professors = professorService.findBySchool(school)



        val professorDif = course.professors.filter { it !in professors }

        if (professorDif.isNotEmpty())
        {
            try { professorService.saveAll(professorDif) } catch (e: Exception) {
                logger.error("Error occurred while trying to save professors", e)
                return null
            }
        }

        val role = guild.createRole().setColor(ThreadLocalRandom.current().nextInt(0xFFFFF))
            .setName(courseModel.name.replace(regex, "-").lowercase()).await()

        val channel = guild.createTextChannel(courseModel.name.replace(regex, "-").lowercase())
            .addPermissionOverride(role, Permission.ALL_CHANNEL_PERMISSIONS, 0L)
            .addPermissionOverride(guild.publicRole, 0L, Permission.ALL_CHANNEL_PERMISSIONS).await()

        course.apply {
            channelId = channel.idLong
            roleId = role.idLong
        }

        return runCatching { classroomRepository.save(course) }
            .onFailure { logger.error("Error has occurred during the save", it); runCleanUp(course, commandEvent, professorDif) }
            .onSuccess { createReminders(commandEvent, courseModel, it) }.getOrNull()
    }

    private suspend fun createReminders(commandEvent: CommandEvent, model: CourseModel, course: Course)
    {
        /*
        val startDate = if (course.startDate.isAfter(Instant.now())) course.startDate else Instant.now()
        val endDate = course.endDate
        val days = model.meetingDays.map { it.uppercase() }

        while (startDate.isBefore(endDate) || startDate == endDate)
        {
            val x = LocalDateTime.ofInstant(startDate, course.school.timeZone)
            val day = x.dayOfWeek.name
            if (!days.contains(day)) continue

            val time = x.toLocalTime()
            val reminders = listOf(
                CourseReminder(course.id, time.minusHours(1), "1 hour before"),
            )
            val reminder = CourseReminder(
                course = course,
            )


        }
         */
    }





    open fun findCoursesByGuild(guildId: Long): Set<Course> =
        runCatching { classroomRepository.findByGuildIdEquals(guildId) }.onFailure {
            logger.error(
                "Error has occurred while trying to get the courses for guild id: {}",
                guildId,
                it
            )
        }.getOrThrow()


    private suspend fun runCleanUp(
        course: Course, event: CommandEvent, professors: Collection<Professor> = mutableSetOf()
    )
    {
        withContext(dispatcher)
        {
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
            }, { logger.error("Error has occurred while attempt to delete channel during clean up.", it) }) ?: logger.warn("{}'s channel does not exist", course.name)

           try { professorService.removeAll(professors = professors) } catch (e: Exception) { logger.error("Error has occurred while removing professors", e) }
        }
    }


    open suspend fun findDuplicateCourse(name: String, number: Long, termId: String) = withContext(dispatcher) {
        classroomRepository.findCourseByNameAndNumberAndTermIdentifier(name, number, termId)
    }


    open fun getClassesInGuild(guildId: Long): Set<Course> =
        runCatching { classroomRepository.findByGuildIdEquals(guildId) }
            .onFailure { logger.error("Error has occurred while trying to get the courses for guild id: {}", guildId, it) }
            .getOrThrow()

    open fun findEmptyClassesInGuild(guildId: Long): List<Course> =
        runCatching { classroomRepository.findByAssignmentsIsEmptyAndGuildIdEquals(guildId) }
            .onFailure { logger.error("Error has o") }
            .getOrThrow()

    open fun getClassesBySchool(school: School): Set<Course> =
        runCatching { classroomRepository.findBySchool(school = school) }
            .onFailure { logger.error("Error has occurred while trying to get the courses for school id: {}", school.name, it) }
            .getOrThrow()

}