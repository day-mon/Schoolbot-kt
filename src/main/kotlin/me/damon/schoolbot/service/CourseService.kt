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
import me.damon.schoolbot.objects.school.CourseReminder
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.Permission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
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
    @Autowired
    lateinit var courseReminderService: CourseReminderService

    private val logger by SLF4J
    private val regex = Regex("\\s+")

    open suspend fun deleteCourse(course: Course, commandEvent: CommandEvent) =
        runCatching { courseReminderService.deleteAllByCourse(course); classroomRepository.delete(course);  }
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
            school = school, professorService = professorService
        )

        val professors = professorService.findBySchool(school)


        val professorDif = course.professors.filter { it !in professors }

        if (professorDif.isNotEmpty())
        {
            try
            {
                professorService.saveAll(professorDif)
            } catch (e: Exception)
            {
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

        return runCatching { classroomRepository.save(course) }.onFailure {
                logger.error("Error has occurred during the save", it); runCleanUp(
                course,
                commandEvent,
                professorDif
            )
            }.getOrNull()
    }

    @Throws(Exception::class)
    fun createReminders(commandEvent: CommandEvent, course: Course): List<CourseReminder>
    {
        val timeZone = if (course.school.isPittSchool) "America/New_York" else course.school.timeZone
        val startDate = LocalDateTime.ofInstant(course.startDate, ZoneId.of(timeZone))
        val endDate = LocalDateTime.ofInstant(course.endDate, ZoneId.of(timeZone))
        val meetingDays =  course.meetingDays

        if (meetingDays.split(",").isEmpty())
        {
            logger.error("No meeting days found for {} or they are not stored in a correct format", course.name)
            return listOf()
        }

        var startDateIt = if (startDate.isBefore(LocalDateTime.now())) LocalDateTime.now() else startDate

        val days = meetingDays.split(",").map { it.uppercase().trim() }
        val reminderList = mutableListOf<CourseReminder>()

        while (startDateIt.isBefore(endDate) || startDateIt.isEqual(endDate))
        {
            val day = startDateIt.dayOfWeek.name.uppercase()
            if (day !in days)  { startDateIt = startDateIt.plusDays(1); continue } // only for the first iteration

            startDateIt = if (days[days.size - 1] == day)
            {
                val start = days[0]
                startDateIt.with(TemporalAdjusters.next(DayOfWeek.valueOf(start)))
            }
            else
            {
                val next = days.indexOf(day) + 1
                startDateIt.with(TemporalAdjusters.next(DayOfWeek.valueOf(days[next])))

            }

            reminderList.addAll(
                listOf(
                    CourseReminder(course = course, remindTime = startDateIt.minusMinutes(60)),
                    CourseReminder(course = course, remindTime = startDateIt.minusMinutes(30)),
                    CourseReminder(course = course, remindTime = startDateIt.minusMinutes(10)),
                    CourseReminder(course = course, remindTime = startDateIt)
                ),
            )

            logger.info("DATE IS {}", startDateIt)
        }

        // todo: look into how to improve this

       return courseReminderService.saveAll(reminderList)

    }


    // update course
    open fun update (course: Course): Course = runCatching { classroomRepository.save(course) }
        .onFailure { logger.error("Error has occurred while trying to update {} ", course.name, it) }
        .getOrThrow()



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