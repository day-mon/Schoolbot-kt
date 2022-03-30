package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.*

@Entity(name = "Professor")
@Table(name = "professors")
@Transactional
open class Professor(
    @Column(name = "firstName", nullable = false, columnDefinition = "TEXT")
    open val firstName: String,

    @Column(name = "lastName", nullable = false, columnDefinition = "TEXT")
    open val lastName: String,

    @Column(name = "emailPrefix", nullable = false, columnDefinition = "TEXT")
    open val emailPrefix: String = lastName,



    @ManyToOne
    @JoinColumn(name = "school_id")
    open val school: School,


    @ManyToMany(fetch = FetchType.LAZY )
    @JoinTable(
        name = "professors_courses",
        joinColumns = [JoinColumn(name = "professor_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "course_id", referencedColumnName = "id")]
    )
    open val courses: MutableSet<Course> = mutableSetOf(),

    ) : Pagable, Identifiable
{
    @Id
    @Column(name = "id", unique = true, updatable = false)
    override val id: UUID = UUID.randomUUID()

    @Column(name = "fullName", unique = true)
    open val fullName: String = "$firstName $lastName"

    override fun getAsEmbed(): MessageEmbed = Embed {
        title = "Professor $lastName"
        field("First Name", firstName)
        field("Last Name", lastName)
        field("Full Name", fullName)
        field("School", school.name)

        // field("Course count", courses.size.toString())
    }

    override fun getAsEmbed(guild: Guild): MessageEmbed = Embed {
        title = "Professor $lastName"
        field("First Name", firstName)
        field("Last Name", lastName)
        field("Full Name", fullName)
        field("School", school.name)
      //  field("Course count", courses.size.toString())
        color = guild.getRoleById(school.roleId)?.colorRaw

    }
}
