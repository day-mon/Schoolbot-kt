package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.ext.toDiscordTimeZone
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed
import org.hibernate.annotations.GenericGenerator
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "assignments")
@Entity(name = "assignment")
@Transactional
class Assignment (

    @Column(name = "name")
     var name: String,

    @Column(name= "description")
     var description: String,

    @Column(name = "points")
     var points: Int,

    @Column(name = "dueDate")
     var dueDate: Instant,


    @ManyToOne
    @JoinColumn(name = "course", nullable = false)
     val course: Course,

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
     val reminders: List<AssignmentReminder> = emptyList()

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
        field("Due Date", dueDate.toDiscordTimeZone(), true)
    }
}
