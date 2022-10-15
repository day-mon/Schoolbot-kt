package me.damon.schoolbot.objects.school

import java.time.Duration

private val longTermTypeReminders = listOf(Duration.ofDays(3), Duration.ofDays(2), Duration.ofDays(1), Duration.ofHours(6), Duration.ofHours(1), Duration.ofMinutes(10), Duration.ofMinutes(0))
private val normalTypeReminders = listOf(Duration.ofDays(1), Duration.ofHours(6), Duration.ofHours(1), Duration.ofMinutes(10), Duration.ofMinutes(0))

enum class AssignmentType(val offsets: List<Duration>)
{
    QUIZ(longTermTypeReminders),
    EXAM(longTermTypeReminders),
    PROJECT(longTermTypeReminders),
    HOMEWORK(normalTypeReminders),
    OTHER(normalTypeReminders);

    // getter for the offsets



    override fun toString(): String
    {
        return when(this)
        {
            QUIZ -> "Quiz"
            EXAM -> "Exam"
            PROJECT -> "Project"
            HOMEWORK -> "Homework"
            OTHER -> "Other"
        }
    }

    companion object
    {
        fun fromString(string: String): AssignmentType
        {
            return when(string.lowercase())
            {
                "quiz" -> QUIZ
                "exam" -> EXAM
                "project" -> PROJECT
                "homework" -> HOMEWORK
                "other" -> OTHER
                else -> OTHER
            }
        }
    }



}