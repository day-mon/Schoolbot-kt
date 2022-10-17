package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.ext.toDiscordTimeZone
import me.damon.schoolbot.ext.toDiscordTimeZoneLDST
import me.damon.schoolbot.ext.toDiscordTimeZoneRelative
import me.damon.schoolbot.ext.toTitleCase
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

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY) var reminders: List<AssignmentReminder> = emptyList(),

    @Column(name="type")
    var assignmentType: AssignmentType

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
        title = this@Assignment.name
        description = this@Assignment.description
        field("Points", points.toString(), true)
        field("Due Date", dueDate.toDiscordTimeZone(), true)
        field("Type", assignmentType?.name ?: "None", true)
        try
        {
            field(
                "Reminders", reminders.joinToString(separator = "\n",
                    transform = { "${it.remindTime.toDiscordTimeZoneLDST()} (${it.remindTime.toDiscordTimeZoneRelative()})" })
            )
        } catch (e: Exception) { }

    }

    suspend fun getInitialReminders(): List<AssignmentReminder> =
        this.assignmentType.offsets
        .map { this.dueDate.minusMillis(it.toMillis()) }
        .map { AssignmentReminder(this, it) }

}
