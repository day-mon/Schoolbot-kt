package me.damon.schoolbot.objects.school

import me.damon.schoolbot.objects.misc.Pagintable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "assignments")
@Entity(name = "Assignment")
class Assignment (
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    val id: UUID,

    @Column(name = "name")
    val name: String,

    @Column(name= "description")
    val description: String,

    @Column(name = "points")
    val points: Int,

    @Column(name = "dueDate")
    val dueDate: Instant,

    @OneToOne(mappedBy = "id", cascade = [CascadeType.ALL])
    val classroom: Classroom



) : Comparable<Assignment>, Pagintable
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
