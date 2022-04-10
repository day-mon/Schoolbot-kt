package me.damon.schoolbot.objects.school

import me.damon.schoolbot.ext.empty
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "assignment_reminders")
@Entity(name = "AssignmentReminder")
open class AssignmentReminder(
    @Id
    open val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    open val assignment: Assignment,

    open val remindTime: Instant,

    open val message: String = String.empty
)