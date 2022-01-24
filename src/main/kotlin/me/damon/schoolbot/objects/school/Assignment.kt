package me.damon.schoolbot.objects.school

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Table(name = "assignments")
@Entity(name = "Assignment")
data class Assignment (
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
    val dueDate: LocalDateTime,

    @OneToOne(mappedBy = "id", cascade = [CascadeType.ALL])
    val classroom: Classroom



) : Comparable<Assignment>
{
    override fun compareTo(other: Assignment): Int
    {
        return dueDate.compareTo(other.dueDate)
    }
}
