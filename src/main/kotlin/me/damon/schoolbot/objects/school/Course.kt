package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "Courses")
@Entity(name = "courses")

class Course(
    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String,

    @Column(name = "prerequisite")
    val prerequisite: String,

    @ManyToMany(mappedBy = "courses", fetch = FetchType.EAGER)
    val professors: MutableSet<Professor>,

    @OneToMany(mappedBy = "id", fetch = FetchType.EAGER)
    val assignments: MutableSet<Assignment>,

    @Column(name = "startDate", nullable = false)
    val startDate: Instant,

    @Column(name = "endDate", nullable = false)
    val endDate: Instant,

    //@Column(name = "term")
    //val term: ClassTerm,

    @Column(name = "termId")
    val termIdentifier: String = String.empty,

    @Column(name = "url")
    val url: String,

    @Column(name = "number")
    val number: Long,

    @Column(name = "subjectAndIdentifier", nullable = true)
    val subjectAndIdentifier: String,

    @Column(name = "roleId", nullable = true)
    var roleId: Long = 0,

    @Column(name = "channelId", nullable = true)
    var channelId: Long = 0,

    @Column(name = "guildId", nullable = false)
    val guildId: Long,

    @Column(name = "autoFilled")
    /**
     * A class that was auto populated via api
     */
    val autoFilled: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "school_id")
    val school: School,

    @Id
    private val id: UUID = UUID.nameUUIDFromBytes((name+termIdentifier+guildId+number).toByteArray())

    ) : Pagable

{
    override fun getAsEmbed(): MessageEmbed = Embed {
        title = "$name | $subjectAndIdentifier"
        url = this@Course.url

        field {
            name = "Description"
            value = this@Course.description
        }

        field {
            name = "Prerequisites"
            value = prerequisite
        }

        field {
            name = "Term"
            value = termIdentifier
        }

        field {
            name = "Class Number"
            value = number.toString()
            inline = true
        }

        field {
            name = "Professors"
            value = professors.joinToString { "`${it.firstName}, ${it.lastName}`" }
        }

        field {
            name = "Assignment Count"
            value = assignments.size.toString()
        }
    }


     fun getAsEmbed(guild: Guild): MessageEmbed = Embed {
         val role = guild.getRoleById(roleId)
         val channel = guild.getTextChannelById(channelId)

        title = "$name | $subjectAndIdentifier"
        url = this@Course.url

        field {
            name = "Description"
            value = this@Course.description
        }

        field {
            name = "Prerequisites"
            value = prerequisite
        }

        field {
            name = "Term"
            value = termIdentifier
        }

        field {
            name = "Class Number"
            value = number.toString()
            inline = true
        }

        field {
            name = "Professors"
            value = professors.joinToString { "`${it.firstName}, ${it.lastName}`" }
        }

        field {
            name = "Assignment Count"
            value = assignments.size.toString()
            inline = true
        }

         field {
             name = "Channel"
             value = channel?.asMention ?: "N/A"
         }

         field {
             name = "Role"
             value = role?.asMention ?: "N/A"
         }

         color = role?.colorRaw ?: 0xFFFF

    }
}




