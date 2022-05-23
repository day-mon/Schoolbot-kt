package me.damon.schoolbot.objects.school

import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.ext.minus
import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import java.util.*
import javax.persistence.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes


fun defaultReminders(assignment: Assignment): List<AssignmentReminder> = listOf(
        AssignmentReminder(assignment, assignment.dueDate.minus(1.days)),
        AssignmentReminder(assignment, assignment.dueDate.minus(6.hours)),
        AssignmentReminder(assignment, assignment.dueDate.minus(1.hours)),
        AssignmentReminder(assignment, assignment.dueDate.minus(10.minutes)),
        AssignmentReminder(assignment, assignment.dueDate)
)



@Table(name = "assignment_reminders")
@Entity(name = "AssignmentReminder")
open class AssignmentReminder(
    @ManyToOne
    @JoinColumn(name = "assignment")
    open val assignment: Assignment,

    open var remindTime: Instant,

    open val message: String = String.empty
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