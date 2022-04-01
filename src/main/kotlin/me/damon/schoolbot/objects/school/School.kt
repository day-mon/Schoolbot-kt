package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.hibernate.Hibernate
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.persistence.*

@Entity(name = "School")
@Table(name = "schools")
@Transactional
open class School(
    @Id
    @Column(name = "id", unique = true, updatable = false)
    override val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, unique = true)
    open var name: String,

    @Column(name = "url", nullable = true)
    open var url: String,

    @Column(name = "emailSuffix", nullable = false)
    open var emailSuffix: String,

    @Column(name = "isPittSchool", nullable = false )
    open val isPittSchool: Boolean = name.contains("University of Pittsburgh"),

    @Column(name = "guildId", nullable = false)
    open var guildId: Long = -1L,

    @Column(name = "roleId", nullable = false)
    open var roleId: Long = -1L,

    @OneToMany(mappedBy = "school", fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)

    open val professor: MutableSet<Professor>,

    @OneToMany(mappedBy = "school", fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)
    open val classes: MutableSet<Course>,

    @Column(name = "timeZone", nullable = false, updatable = true)
    open val timeZone: ZoneId

    ) : Pagable, Identifiable
{

//    fun hasProfessors() = professor.isNotEmpty()
    override fun getAsEmbed(): MessageEmbed = Embed {
        title = name
        url = if (url.isNullOrEmpty()) "https://schoolbot.dev" else url



        field {
            name = "Email"
            value = emailSuffix
            inline = false
        }

        color = ThreadLocalRandom.current().nextInt(0xFFFFFF)
    }


    override fun getAsEmbed(guild: Guild): MessageEmbed = Embed {
        title = name
        url = if (url.isNullOrEmpty()) "https://schoolbot.dev" else url


        field {
            name = "Email"
            value = emailSuffix
            inline = false
        }

        color = guild.getRoleById(roleId)?.colorRaw ?:  ThreadLocalRandom.current().nextInt(0xFFFFFF)
    }
    override fun toString(): String = name
}


