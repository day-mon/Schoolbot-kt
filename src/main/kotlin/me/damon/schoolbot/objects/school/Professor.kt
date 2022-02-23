package me.damon.schoolbot.objects.school

import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.*
import javax.persistence.*

@Entity(name = "Professor")
@Table(name = "professors")
class Professor(
    @Id
    @Column(name = "id", unique = true, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "firstName", nullable = false, columnDefinition = "TEXT")
    val firstName: String,

    @Column(name = "lastName", nullable = false, columnDefinition = "TEXT")
    val lastName: String,

    @Column(name = "emailPrefix", nullable = false, columnDefinition = "TEXT")
    val emailPrefix: String,

    @ManyToOne
    @JoinColumn(name = "school_id")
    val school: School,


    @ManyToMany
    @JoinTable(
        name = "professors_courses",
        joinColumns = [JoinColumn(name = "professor_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "course_id", referencedColumnName = "id")]
    )
    val courses: Set<Course>



    ) : Pagable
{
    override fun getAsEmbed(): MessageEmbed
    {
        TODO("Not yet implemented")
    }
}
