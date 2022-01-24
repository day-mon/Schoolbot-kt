package me.damon.schoolbot.objects.school

import me.damon.schoolbot.objects.misc.Pagintable
import java.util.*
import javax.persistence.*

@Entity(name = "Professor")
@Table(name = "professors")
data class Professor(
    @Id
    @Column(name = "id", unique = true, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "firstName", nullable = false, columnDefinition = "TEXT")
    val firstName: String,

    @Column(name = "lastName", nullable = false, columnDefinition = "TEXT")
    val lastName: String,

    @Column(name = "emailPrefix", nullable = false, columnDefinition = "TEXT")
    val emailPrefix: String,

    @ManyToMany(mappedBy = "id", cascade = [(CascadeType.ALL)])
    val classes: Set<Classroom>
    ) : Pagintable
