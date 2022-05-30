package me.damon.schoolbot.objects.school

import me.damon.schoolbot.ext.empty
import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "course_reminders")
@Entity(name = "CourseReminder")
class CourseReminder(
    @ManyToOne
    @JoinColumn(name = "course")
     val course: Course,

     var remindTime: Instant,

     val specialMessage: String = String.empty
) {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
     var id: UUID? = null

}
