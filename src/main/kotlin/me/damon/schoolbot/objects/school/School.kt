package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.ZoneId
import java.util.*
import javax.persistence.*

@Entity(name = "School")
@Table(name = "schools")
class  School(
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
    val isPittSchool: Boolean = name.contains("University of Pittsburgh"),

    @Column(name = "guildId", nullable = false)
    var guildId: Long = -1L,

    @Column(name = "roleId", nullable = false)
    var roleId: Long = -1L,


    @OneToMany(mappedBy = "id")
    val professor: Set<Professor>,


    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL])
    val classes: Set<Course>,


    @Column(name = "timeZone", nullable = false, updatable = true)
    val timeZone: ZoneId

    ) : Pagable
{
    override fun getAsEmbed(): MessageEmbed = Embed {
        title = name
        field {
            name = "Email Suffix"
            value = emailSuffix
        }


        field {
            name = "Classes Count"
            value = classes.size.toString()
        }

        field {
            name = "Professors Count"
            value  = professor.size.toString()
        }

        color = Random().nextInt(0xFFFF)
    }
}


