package me.damon.schoolbot.objects.models

import com.fasterxml.jackson.annotation.JsonIgnore
import me.damon.schoolbot.ext.empty
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
    val description: String,
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
        description = description,
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

        val regex = Regex("\\s+")


        val profs: MutableList<Professor> = mutableListOf()

        for (professor in professors)
        {
            if (professor == "To be Announced")
            {
                profs.add(emptyProfessor(school))
                continue
            }
            val professorToAdd =
                if (professor.contains(regex)) handleCorrectPattern(professor, school, professorService) ?: continue
                else handleIncorrectPattern(school, professor)
            profs.add(professorToAdd)
        }
        return profs
    }


    private suspend fun handleCorrectPattern(professor: String, school: School, professorService: ProfessorService): Professor?
    {
        val regex = Regex("\\s+")

        val split = professor.split(
            regex = regex,
            limit = 2
        )
        val newProfessor = Professor(
            firstName = split[0],
            lastName = split[1],
            school = school,
        )

        return try
        {
            val professorFound = professorService.findBySchoolName(name, school.guildId)
            if (professorFound.isEmpty()) newProfessor
            else professorFound.first()
        }
        catch (e: Exception) { newProfessor }


    }

    private fun handleIncorrectPattern(school: School, professorName: String): Professor
    {
    return school.professor.find { it.firstName == professorName && it.lastName == String.empty  } ?: Professor(
            firstName =  professorName,
            lastName = String.empty,
            courses = listOf(),
            emailPrefix = "unknown",
            school = school
        )

    }

}