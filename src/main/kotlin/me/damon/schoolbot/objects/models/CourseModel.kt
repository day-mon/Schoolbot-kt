package me.damon.schoolbot.objects.models


import com.google.gson.annotations.SerializedName
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.Professor
import me.damon.schoolbot.objects.school.School
import java.time.Instant

data class CourseModel(
    @SerializedName("campus")
    val campus: String, // Pittsburgh Campus
    @SerializedName("career")
    val career: String,
    @SerializedName("classAttributes")
    val classAttributes: Any?, // null
    @SerializedName("classCapacity")
    val classCapacity: Int, // 20
    @SerializedName("classNumber")
    val classNumber: Int, // 28920
    @SerializedName("components")
    val components: String, // Lecture Required, Recitation Required
    @SerializedName("description")
    val description: String, // This course gives a broad introduction to contemporary work on the philosophy of mind.  It will primarily focus on the nature of consciousness, the mind-body problem, and may include how we know the minds of other human beings, the nature of personal identity over time, as well as discussion of the theory of action.
    @SerializedName("dropConsent")
    val dropConsent: Any?, // null
    @SerializedName("endDate")
    val endDate: String, // 2022-04-22T09:50:00
    @SerializedName("enrollmentRequirements")
    val enrollmentRequirements: Any?, // null
    @SerializedName("grading")
    val grading: String,
    @SerializedName("identifier")
    val identifier: String, // PHIL 0460 - 1040
    @SerializedName("instructors")
    val instructors: List<String>,
    @SerializedName("location")
    val location: String, // Pittsburgh Campus
    @SerializedName("meetingDays")
    val meetingDays: List<String>,
    @SerializedName("name")
    val name: String, // INTRODUCTION TO PHILOSOPHY OF MIND
    @SerializedName("restrictedSeats")
    val restrictedSeats: Int, // 0
    @SerializedName("room")
    val room: String, // 2322 Cathedral of Learning
    @SerializedName("seatsOpen")
    val seatsOpen: Int, // 0
    @SerializedName("seatsTaken")
    val seatsTaken: Int, // 20
    @SerializedName("session")
    val session: String, // Academic Term
    @SerializedName("startDate")
    val startDate: String, // 2022-01-10T09:00:00
    @SerializedName("status")
    val status: String, // Closed
    @SerializedName("units")
    val units: Int, // 0
    @SerializedName("unrestrictedSeats")
    val unrestrictedSeats: Int, // 0
    @SerializedName("waitListCapacity")
    val waitListCapacity: Int, // 20
    @SerializedName("waitListTotal")
    val waitListTotal: Int, // 0

    var term: String = String.empty,
    var url: String = String.empty
) {
    fun asCourse(school: School)
    = Course(
        name = name,
        description = description,
        number = classNumber.toLong(),
        autoFilled = true,
        school = school,
        termIdentifier = term,
        prerequisite = (enrollmentRequirements ?: "N/A") as String,
        assignments = mutableSetOf(),
        subjectAndIdentifier = identifier,
        guildId = school.guildId,
        professors = processProfessors(instructors, school),
        startDate = Instant.parse("${startDate}Z").minusMillis(14400000),
        endDate = Instant.parse("${endDate}Z").minusMillis(14400000),
        url = url
    )

    private fun processProfessors(professors: List<String>, school: School): MutableSet<Professor>
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
                        courses = setOf(),
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

                val prof = school.professor.find { it.firstName == split[0] && it.lastName == split[1]  }


                if (prof != null)
                {
                    profs.add(prof)
                    continue
                }


                profs.add(
                    Professor(
                        firstName = split[0],
                        lastName = split[1],
                        courses = setOf(),
                        emailPrefix = split[1],
                        school = school
                    )
                )
            }
            //TODO: Fix TBA
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
                        courses = setOf(),
                        emailPrefix = "unknown",
                        school = school
                    )
                )
            }
        }
        return profs
    }
}