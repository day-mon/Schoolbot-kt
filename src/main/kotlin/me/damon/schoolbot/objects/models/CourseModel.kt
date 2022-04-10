package me.damon.schoolbot.objects.models

import com.fasterxml.jackson.annotation.JsonIgnore
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.repository.ProfessorRepository
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
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
    fun asCourse(school: School, professorRepository: ProfessorRepository)
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
        professors = processProfessors(instructor, school, professorRepository),
        assignments = mutableSetOf(),
        meetingDays = meetingDays.joinToString { it },
        autoFilled = true,
        school = school
    )
    @JsonIgnore
    private fun processProfessors(professors: List<String>, school: School, professorRepository: ProfessorRepository): MutableSet<Professor>
    {
        val regex = Regex("\\s+")
        if (professors.isEmpty()) return mutableSetOf(
            Professor(
                firstName = "N/A",
                lastName = "N/A",
                emailPrefix = "N/A",
                school = school,
                courses = mutableSetOf()
            )
        )

        val profs: MutableSet<Professor> = mutableSetOf()

        for (professor in professors)
        {
            if (professor == "To be Announced")
            {
                profs.add(
                    Professor(
                        firstName = "To be Announced",
                        lastName = String.empty,
                        courses = mutableSetOf(),
                        school = school,
                        emailPrefix = "tba"
                    )
                )
                continue
            }
            if (professor.contains(regex))
            {
                val split = professor.split(
                    regex = regex,
                    limit = 2
                )

                val duplicate = professorRepository.findByFullNameEqualsIgnoreCaseAndSchool_GuildIdEquals("${split[0]} ${split[1]}", school.guildId)
                    .orElse(null)


                if (duplicate != null)
                {
                    profs.add(duplicate)
                    continue
                }

                profs.add(
                    Professor(
                        firstName = split[0],
                        lastName = split[1],
                        courses = mutableSetOf(),
                        emailPrefix = split[1],
                        school = school
                    )
                )
            }
            else
            {
                val prof = school.professor.find { it.firstName == professor && it.lastName == String.empty  }


                if (prof != null)
                {
                    profs.add(prof)
                    continue
                }

                profs.add(
                    Professor(
                        firstName =  professor,
                        lastName = String.empty,
                        courses = mutableSetOf(),
                        emailPrefix = "unknown",
                        school = school
                    )
                )
            }
        }
        return profs
    }
}