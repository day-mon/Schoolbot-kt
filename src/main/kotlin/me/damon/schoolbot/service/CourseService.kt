package me.damon.schoolbot.service


import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.plus
import me.damon.schoolbot.objects.models.CourseModel
import me.damon.schoolbot.objects.repository.ClassroomRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.CourseReminder
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@Service("CourseService")
 class CourseService(
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

     suspend fun deleteCourse(course: Course, guild: Guild) =
        runCatching { courseReminderService.deleteAllByCourse(course); classroomRepository.delete(course);  }
        .onFailure { logger.error("Error has occurred while trying to delete {} ", course.name, it) }
        .onSuccess { runCleanUp(course, guild) }
        .getOrThrow()

     fun deleteCourse(course: Course, jda: JDA) = runCatching { classroomRepository.delete(course) }
        .onFailure { logger.error("Error has occurred while trying to delete {}", course.name) }
        .getOrThrow()

    fun refactorRemindersByCourse(course: Course) = runCatching { courseReminderService.deleteAllByCourse(course); createReminders(course) }
        .onFailure { logger.error("Error has occurred while trying to refactor courses") }
        .getOrThrow()

    fun createReminderOnDay(days: List<DayOfWeek>, course: Course)
    {
        val school = course.school
        val zone = school.zone
        val startDate = LocalDateTime.ofInstant(course.startDate, zone)
        val endDate = LocalDateTime.ofInstant(course.endDate, zone)

        var startingDateIt =
            if (startDate.isBefore(LocalDateTime.now(zone)))
            {
                logger.debug("Changing reminder start date iteration to current day due to start date already passing.. ")
                LocalDateTime.of(LocalDate.now(), startDate.toLocalTime())
            }
            else startDate

        val reminders = mutableListOf<CourseReminder>()

        while (startingDateIt.isBefore(endDate) || startingDateIt.isEqual(endDate))
        {
            val day = startDate.dayOfWeek
            if (day !in days) { startingDateIt += 1.days; continue }
            val offset = zone.rules.getOffset(startingDateIt)

            reminders.addAll(
                listOf(
                    CourseReminder(course = course, remindTime = startingDateIt.minusHours(1).toInstant(offset)),
                    CourseReminder(course = course, remindTime = startingDateIt.minusMinutes(30).toInstant(offset)),
                    CourseReminder(course = course, remindTime = startingDateIt.minusMinutes(10).toInstant(offset)),
                    CourseReminder(course = course, remindTime = startingDateIt.toInstant(offset)),
                )
            )



            startingDateIt =  if (days.last() == day)
            {
                val start = days.first()
                startingDateIt.with(TemporalAdjusters.next(start))
            }
            else
            {
                val next = days.indexOf(day) + 1
                startingDateIt.with(TemporalAdjusters.next(days[next]))
            }
        }
        courseReminderService.saveAll(reminders)


    }

    suspend fun createPittCourse(
        guild: Guild,
        school: School,
        courseModel: CourseModel
    ): Course
    {
        val course = courseModel.asCourse(
            school = school, professorService = professorService
        )

        val professors = professorService.findBySchool(school)

        val professorDif = course.professors.filter { it !in professors }

        if (professorDif.isNotEmpty())
        {
            professorService.saveAll(professorDif)
        }

        val role = guild.createRole().setColor(Random.nextInt(0xFFFFF))
            .setName(courseModel.name.replace(Constants.SPACE_REGEX, "-").lowercase()).await()

        val channel = guild.createTextChannel(courseModel.name.replace(Constants.SPACE_REGEX, "-").lowercase())
            .addPermissionOverride(role, Permission.ALL_CHANNEL_PERMISSIONS, 0L)
            .addPermissionOverride(guild.publicRole, 0L, Permission.ALL_CHANNEL_PERMISSIONS).await()

        course.apply {
            channelId = channel.idLong
            roleId = role.idLong
        }

        return runCatching { classroomRepository.save(course) }
            .onFailure { logger.error("Error has occurred during the save", it); runCleanUp(course, guild, professorDif) }
            .getOrThrow()
    }

    fun createReminders(course: Course)
    {
        val startTime = System.currentTimeMillis()
        logger.info("Starting to create reminders for {}", course.name)

        val school = course.school
        val zone =  school.zone
        val startDate = LocalDateTime.ofInstant(course.startDate, zone)
        val endDate = LocalDateTime.ofInstant(course.endDate, zone)
        val meetingDays = course.meetingDays

        if (meetingDays.split(",").isEmpty())
            return logger.error("No meeting days found for {} or they are not stored in a correct format", course.name)

        var startDateIt =
            if (startDate.isBefore(LocalDateTime.now(zone)))
            {
                logger.debug("Changing reminder start date iteration to current day due to start date already passing.. ")
                LocalDateTime.of(LocalDate.now(), startDate.toLocalTime())
            }
            else startDate

        val days = meetingDays.split(",").map { it.uppercase().trim() }
        val reminders = mutableListOf<CourseReminder>()


        while (startDateIt.isBefore(endDate) || startDateIt.isEqual(endDate))
        {
            val day = startDateIt.dayOfWeek.name.uppercase()
            if (day !in days)  { startDateIt += 1.days; logger.debug("Skipped day: {}", startDateIt); continue } // only for the first iteration

            val offset = school.zone.rules.getOffset(startDateIt)

            reminders.addAll(
                listOf(
                    CourseReminder(course = course, remindTime = startDateIt.minusHours(1).toInstant(offset)),
                    CourseReminder(course = course, remindTime = startDateIt.minusMinutes(30).toInstant(offset)),
                    CourseReminder(course = course, remindTime = startDateIt.minusMinutes(10).toInstant(offset)),
                    CourseReminder(course = course, remindTime = startDateIt.toInstant(offset)),
                )
            )



            startDateIt = if (days.last() == day)
            {
                val start = days.first()
                val nextDayOfWeek = DayOfWeek.valueOf(start)
                startDateIt.with(TemporalAdjusters.next(nextDayOfWeek))
            }
            else
            {
                val next = days.indexOf(day) + 1
                val nextDayOfWeek = DayOfWeek.valueOf(days[next])
                startDateIt.with(TemporalAdjusters.next(nextDayOfWeek))

            }
        }

        courseReminderService.saveAll(reminders)
        logger.info("Reminder sequence for {} has concluded it took {}s", course.name, Duration.ofMillis(System.currentTimeMillis() - startTime).toSeconds())
    }


     fun update (course: Course): Course = runCatching { classroomRepository.save(course) }
        .onFailure { logger.error("Error has occurred while trying to update {} ", course.name, it) }
        .getOrThrow()



     suspend fun findAllByGuild(guildId: Long): List<Course> =
        runCatching { classroomRepository.findAllByGuild(guildId).await() }
            .onFailure { logger.error("Error has occurred while trying to get the courses for guild id: {}", guildId, it) }
            .getOrThrow()



    private fun courseRoleAndChannelCleanUp(course: Course, guild: Guild) {

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
    }

    private suspend fun runCleanUp(
        course: Course,
        guild: Guild,
        professors: Collection<Professor> = mutableSetOf()
    )
    {
            withContext(dispatcher)
            {
                courseRoleAndChannelCleanUp(course, guild)
                try { professorService.removeAll(professors = professors) }
                catch (e: Exception) { logger.error("Error has occurred while removing professors", e) }
            }
    }

     suspend fun findDuplicateCourse(number: Long, termId: String) = withContext(dispatcher) {
        classroomRepository.findByNumberAndIdentifier(number, termId).await()
    }


     suspend fun findEmptyAssignmentsInGuild(guildId: Long): List<Course> =
        runCatching { classroomRepository.findAllByEmptyAssignmentsInGuild(guildId).await() }
            .onFailure { logger.error("Error has occurred while searching for the classes with no assignments") }
            .onSuccess { logger.debug("CourseService#findEmptyClassesInGuild has returned a size of {}", it.size) }
            .getOrThrow()

     suspend fun findEmptyAssignmentsBySchoolInGuild(school: School): List<Course> =
        runCatching { classroomRepository.findByNonEmptyAssignmentsInSchool(school).await() }
            .onFailure { logger.error("Error has occurred while searching for the classes with no assignments") }
            .onSuccess { logger.debug("CourseService#findEmptyAssignmentsBySchoolInGuild has returned a size of {}", it.size) }
            .getOrThrow()


     suspend fun findBySchool(school: School): List<Course> =
        runCatching { classroomRepository.findBySchool(school).await() }
            .onFailure { logger.error("Error has occurred while trying to get the courses for school id: {}", school.name, it) }
            .getOrThrow()

}