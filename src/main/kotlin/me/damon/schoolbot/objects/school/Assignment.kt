package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.ext.formatDate
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed
import org.hibernate.annotations.GenericGenerator
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Table(name = "assignments")
@Entity(name = "assignment")
@Transactional
open class Assignment (

    @Column(name = "name")
    open val name: String,

    @Column(name= "description")
    open val description: String,

    @Column(name = "points")
    open val points: Int,

    @Column(name = "dueDate")
    open val dueDate: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "course", nullable = false)
    open val course: Course,

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    open val reminders: List<AssignmentReminder> = emptyList()

    ) : Comparable<Assignment>, Pagable, Identifiable
{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    override var id: UUID? = null
    override fun compareTo(other: Assignment): Int
    =  dueDate.compareTo(other.dueDate)

    override fun getAsEmbed(): MessageEmbed = Embed {
        title = name
        description = this@Assignment.description
        field("Points", points.toString(), true)
        field("Due Date", dueDate.formatDate(), true)
    }
}
