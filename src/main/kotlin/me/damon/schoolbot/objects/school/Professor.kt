package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.*
import javax.persistence.*

@Entity(name = "Professor")
@Table(name = "professors")
class Professor(
    @Column(name = "firstName", nullable = false, columnDefinition = "TEXT")
    val firstName: String,

    @Column(name = "lastName", nullable = false, columnDefinition = "TEXT")
    val lastName: String,

    @Column(name = "emailPrefix", nullable = false, columnDefinition = "TEXT")
    val emailPrefix: String = lastName,

    @ManyToOne
    @JoinColumn(name = "school_id")
    val school: School,

    @ManyToMany
    @JoinTable(
        name = "professors_courses",
        joinColumns = [JoinColumn(name = "professor_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "course_id", referencedColumnName = "id")]
    )
    val courses: Set<Course> = setOf()

    ) : Pagable
{
    @Id
    @Column(name = "id", unique = true, updatable = false)
    private val id: UUID = UUID.randomUUID()

    @Column(name = "fullName", unique = true)
    val fullName: String = "$firstName $lastName"

    override fun getAsEmbed(): MessageEmbed = Embed {
        title = "Professor $lastName"
        field("First Name", firstName)
        field("Last Name", lastName)
        field("Full Name", fullName)
        field("School", school.name)
        field("Course count", courses.size.toString())
    }
}
