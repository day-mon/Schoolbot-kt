package me.damon.schoolbot.objects.school

import me.damon.schoolbot.objects.misc.Pagintable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.ZoneId
import java.util.*
import javax.persistence.*

@Entity(name = "School")
@Table(name = "schools")
data class School(
    @Id
    @Column(name = "id", unique = true, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "url", nullable = true)
    val url: String,

    @Column(name = "emailSuffix", nullable = false)
    val emailSuffix: String,

    @Column(name = "isPittSchool", nullable = false)
    val isPittSchool: Boolean,

    @Column(name = "guildId", nullable = false)
    val guildId: Long = -1L,

    @Column(name = "roleId", nullable = false)
    val roleId: Long = -1L,

    @ManyToMany(mappedBy = "id", cascade = [CascadeType.ALL])
    val professor: Set<Professor> = setOf(),

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL])
    val classes: Set<Classroom> = setOf(),

    @Column(name = "timeZone", nullable = false, updatable = true)
    val timeZone: ZoneId

    ) : Pagintable
{
    override fun getAsEmbed(): MessageEmbed
    {
        TODO("Not yet implemented")

    }
}


