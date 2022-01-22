package me.damon.schoolbot.objects.school

import java.sql.Date
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class Classroom(
    val id: Int,
    val description: String,
    val prerequisite: String,


    val startDate: LocalDateTime,




    val autoFilled: Boolean = false

)
{
    fun setStartDate(startDate: Date)
    {

    }
}