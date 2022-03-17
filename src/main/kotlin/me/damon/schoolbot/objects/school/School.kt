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
    private val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, unique = true)
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
    
    @OneToMany(mappedBy = "school", fetch = FetchType.EAGER)
    val professor: MutableSet<Professor>,

    @OneToMany(mappedBy = "school", fetch = FetchType.EAGER)
    val classes: MutableSet<Course>,
    
    @Column(name = "timeZone", nullable = false, updatable = true)
    val timeZone: ZoneId

    ) : Pagable
{
    override fun getAsEmbed(): MessageEmbed = Embed {
        title = name
        url = if (url.isNullOrEmpty()) "https://schoolbot.dev" else url

        field {
            name = "Classes Count"
            value = classes.size.toString()
            inline = true
        }

        field {
            name = "Professors Count"
            value  = professor.size.toString()
            inline = true
        }

        field {
            name = "Email"
            value = emailSuffix
            inline = false
        }

        color = Random().nextInt(0xFFFF)
    }

}


