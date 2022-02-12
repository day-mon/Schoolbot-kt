package me.damon.schoolbot.objects.school

import me.damon.schoolbot.objects.misc.Pagintable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "classrooms")
@Entity(name = "classroom")

class Classroom(
    @Id
    @Column(name = "id", updatable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description")
    val description: String,

    @Column(name = "prerequisite")
    val prerequisite: String,

    @ManyToMany
    @JoinTable(
        name = "professors",
        joinColumns = [JoinColumn(name = "professor_id")],
        inverseJoinColumns = [JoinColumn(name = "classroom_id")]
        // join on classroom id and professor id
    )
    val professors: Set<Professor>,

    @OneToMany
    val assignments: Set<Assignment>,

    @Column(name = "startDate", nullable = false)
    val startDate: Instant,

    @Column(name = "endDate", nullable = false)
    val endDate: Instant,

    @Column(name = "term")
    val term: ClassTerm,

    @Column(name = "url")
    val url: String,

    @Column(name = "number")
    val number: Long,

    @Column(name = "subjectAndIdentifier", nullable = true)
    val subjectAndIdentifier: String,

    @Column(name = "roleId", nullable = true)
    val roleId: Long,

    @Column(name = "channelId", nullable = true)
    val channelId: Long,

    @Column(name = "guildId", nullable = false)
    val guildId: Long,

    @Column(name = "autoFilled")
    /**
     * A class that was auto populated via api
     */
    val autoFilled: Boolean = false,

    @OneToOne(mappedBy = "id", cascade = [(CascadeType.ALL)])
    val school: School

    ) : Pagintable

{
    enum class ClassTerm { SPRING, WINTER, FALL, SUMMER  }

    override fun getAsEmbed(): MessageEmbed
    {
        TODO("Not yet implemented")
    }
    // TODO: 2022-01-24: Figure out relations for all of the object classes
}




