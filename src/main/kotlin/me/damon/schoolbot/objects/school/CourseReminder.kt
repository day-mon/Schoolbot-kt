package me.damon.schoolbot.objects.school

import org.springframework.data.annotation.Id
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "course_reminders")
@Entity(name = "CourseReminder")
open class CourseReminder(
    @javax.persistence.Id
    open val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "course_id")
    open val course: Course,

    open val remindTime: Instant
)