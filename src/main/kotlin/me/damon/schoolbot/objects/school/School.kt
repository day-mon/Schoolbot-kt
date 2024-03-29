package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.ZoneId
import java.util.*
import javax.persistence.*
import kotlin.random.Random

@Entity(name = "School")
@Table(name = "schools")
class School(
    @Id
    @Column(name = "id", unique = true, updatable = false)
    override val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, unique = false)
     var name: String,

    @Column(name = "url", nullable = true)
     var url: String,

    @Column(name = "emailSuffix", nullable = false)
     var emailSuffix: String,

    @Column(name = "isPittSchool", nullable = false )
     val isPittSchool: Boolean = name.contains("University of Pittsburgh"),

    @Column(name = "guildId", nullable = false)
     var guildId: Long = -1L,

    @Column(name = "roleId", nullable = false)
     var roleId: Long = -1L,

    @OneToMany(mappedBy = "school", fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)
     val professor: List<Professor>,

    @OneToMany(mappedBy = "school", fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)
     val classes: List<Course>,

    @Column(name = "timeZone", nullable = false)
     var timeZone: String,



    ) : Pagable, Identifiable
{

    val zone: ZoneId
    get() = ZoneId.of(timeZone)



    override fun getAsEmbed(): MessageEmbed = Embed {
        title = name
        url = if (url.isNullOrEmpty()) "https://schoolbot.dev" else url



        field {
            name = "Email"
            value = emailSuffix
            inline = false
        }

        color = Random.nextInt(0xFFFFFF)
    }


    override fun getAsEmbed(guild: Guild): MessageEmbed = Embed {
        title = name
        url = if (url.isNullOrEmpty()) "https://schoolbot.dev" else url


        field {
            name = "Email"
            value = emailSuffix
            inline = false
        }
        field(name = "URL", value = this@School.url, inline = true)
        field(name = "Role", value = guild.getRoleById(roleId)?.asMention ?: "N/A", inline = true)
        field(name = "Timezone", value = timeZone, inline = true)


        color = guild.getRoleById(roleId)?.colorRaw ?:  Random.nextInt(0xFFFFFF)
    }
    override fun toString(): String = name
}


