package me.damon.schoolbot.objects.models

import com.fasterxml.jackson.annotation.JsonIgnore
import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.ext.logger
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.objects.school.emptyProfessor
import me.damon.schoolbot.service.ProfessorService
import java.time.Instant

data class CourseModel(
    val campus: String,
    val career: String,
    val classAttributes: String?,
    val classCapacity: Int,
    val classNumber: Int,
    val components: String,
    val courseUrl: String,
    val description: String?,
    val dropConsent: Any?,
    val endDateAndEndTime: Long,
    val enrollmentRequirements: String,
    val grading: String,
    val identifier: String,
    val instructor: List<String>,
    val location: String,
    val meetingDays: List<String>,
    val name: String,
    val restrictedSeats: Int,
    val room: String,
    val seatsOpen: Int,
    val seatsTaken: Int,
    val session: String?,
    val startDateAndStartTime: Long,
    val status: String,
    val topic: String?,
    val units: Int,
    val unrestrictedSeats: Int,
    val waitListCapacity: Int,
    val waitListTotal: Int,
    @JsonIgnore
    var term: String,
) {
    suspend fun asCourse(school: School, professorService: ProfessorService)
    = Course(
        name = name,
        description = description ?: "N/A",
        number = classNumber.toLong(),
        termIdentifier = term,
        prerequisite = enrollmentRequirements,
        subjectAndIdentifier = identifier,
        topic = topic,
        url = courseUrl,
        startDate = Instant.ofEpochMilli(startDateAndStartTime),
        endDate = Instant.ofEpochMilli(endDateAndEndTime),
        guildId = school.guildId, // kinda silly..
        professors = processProfessors(instructor, school, professorService),
        assignments = listOf(),
        meetingDays = meetingDays.joinToString { it },
        autoFilled = true,
        school = school
    )
    @JsonIgnore
    private suspend fun processProfessors(professors: List<String>, school: School, professorService: ProfessorService): List<Professor>
    {
        if (professors.isEmpty()) return listOf(emptyProfessor(school))



        val profs: MutableList<Professor> = mutableListOf()

        for (professor in professors)
        {
            if (professor == "To be Announced")
            {
                val emptyProfessor = emptyProfessor(school)
                val defaultProfessor = professorService.findByNameInSchool(emptyProfessor.fullName, school) ?: emptyProfessor
                profs.add(defaultProfessor)
                continue
            }
            val professorToAdd =
                if (professor.contains(Constants.SPACE_REGEX)) handleCorrectPattern(professor, school, professorService) ?: continue
                else handleIncorrectPattern(school, professor)
            profs.add(professorToAdd)
            logger.debug("{}", professorToAdd.fullName)

        }
        return profs
    }


    private suspend fun handleCorrectPattern(professor: String, school: School, professorService: ProfessorService): Professor?
    {

        val split = professor.split(
            regex = Constants.SPACE_REGEX,
            limit = 2
        )
        val newProfessor = Professor(
            firstName = split.first(),
            lastName = split.last(),
            school = school,
        )

        return try { professorService.findByNameInSchool("${split.first()} ${split.last()}", school) ?: newProfessor }
        catch (e: Exception) { newProfessor }


    }

    private fun handleIncorrectPattern(school: School, professorName: String): Professor =
        school.professor.find { it.firstName == professorName && it.lastName == String.empty  } ?: Professor(
            firstName =  professorName,
            lastName = String.empty,
            courses = listOf(),
            emailPrefix = "unknown",
            school = school
        )
    }