package me.damon.schoolbot.objects.school

import me.damon.schoolbot.ext.empty
import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.persistence.*

@Table(name = "course_reminders")
@Entity(name = "CourseReminder")
open class CourseReminder(
    @ManyToOne
    @JoinColumn(name = "course")
    open val course: Course,

    open val remindTime: Instant,

    open val specialMessage: String = String.empty
) {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null

}
