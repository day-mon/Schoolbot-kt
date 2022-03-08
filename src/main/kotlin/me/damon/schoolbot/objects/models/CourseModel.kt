package me.damon.schoolbot.objects.models


import com.google.gson.annotations.SerializedName

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
    val waitListTotal: Int // 0
)