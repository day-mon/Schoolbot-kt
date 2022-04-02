package me.damon.schoolbot.objects.school

import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "assignments")
@Entity(name = "Assignment")
@Transactional
open class Assignment (
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    override val id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    open val name: String,

    @Column(name= "description")
    open val description: String,

    @Column(name = "points")
    open val points: Int,

    @Column(name = "dueDate")
    open  val dueDate: Instant,

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    open val course: Course,

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    open val assignments: MutableSet<AssignmentReminder> = mutableSetOf(),

    ) : Comparable<Assignment>, Pagable, Identifiable
{
    override fun compareTo(other: Assignment): Int
    {
        return dueDate.compareTo(other.dueDate)
    }

    override fun getAsEmbed(): MessageEmbed
    {
        TODO("Not yet implemented")
    }
}
