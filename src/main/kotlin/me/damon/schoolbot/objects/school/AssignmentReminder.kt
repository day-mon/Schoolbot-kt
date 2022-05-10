package me.damon.schoolbot.objects.school

import me.damon.schoolbot.ext.empty
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Table(name = "assignment_reminders")
@Entity(name = "AssignmentReminder")
open class AssignmentReminder(
    @ManyToOne
    @JoinColumn(name = "assignment")
    open val assignment: Assignment,

    open val remindTime: LocalDateTime,

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