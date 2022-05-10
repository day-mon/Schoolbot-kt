package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.hibernate.annotations.GenericGenerator
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.*

fun emptyProfessor(school: School) = Professor(
    firstName = "N/A",
    lastName = "N/A",
    emailPrefix = "N/A",
    school = school,
    courses = listOf()
)


@Entity(name = "Professor")
@Table(name = "professors")
@Transactional
open class Professor(
    @Column(name = "firstName", nullable = false, columnDefinition = "TEXT")
    open var firstName: String,

    @Column(name = "lastName", nullable = false, columnDefinition = "TEXT")
    open var lastName: String,

    @Column(name = "emailPrefix", nullable = false, columnDefinition = "TEXT")
    open var emailPrefix: String = lastName,

    @ManyToOne
    @JoinColumn(name = "school")
    open val school: School,


    @ManyToMany(fetch = FetchType.LAZY )
    @JoinTable(
        name = "professors_courses",
        joinColumns = [JoinColumn(name = "professor_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "course_id", referencedColumnName = "id")]
    )
    open val courses: List<Course> = listOf(),

    ) : Pagable, Identifiable
{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    override var id: UUID? = null

    @Column(name = "fullName", unique = true)
    open var fullName: String = "$firstName $lastName"

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
        color = guild.getRoleById(school.roleId)?.colorRaw

    }


}
