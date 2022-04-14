package me.damon.schoolbot.objects.school

import me.damon.schoolbot.ext.empty
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Table(name = "course_reminders")
@Entity(name = "CourseReminder")
open class CourseReminder(
    @Id
    open val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "course_id")
    open val course: Course,

    open val remindTime: LocalDateTime,

    open val specialMessage: String = String.empty
)