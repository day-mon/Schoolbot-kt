package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "Courses")
@Entity(name = "courses")
@Transactional
open class Course(
    @Column(name = "name", nullable = false)
    open val name: String,

    @Column(name = "description", columnDefinition = "text")
    open val description: String,

    @Column(name = "prerequisite")
    open val prerequisite: String,



   @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    open val professors: MutableSet<Professor>,

   @OneToMany(mappedBy = "id", fetch = FetchType.LAZY)
    open val assignments: MutableSet<Assignment>,

    @Column(name = "startDate", nullable = false)
    open val startDate: Instant,

    @Column(name = "endDate", nullable = false)
    open val endDate: Instant,

    //@Column(name = "term")
    //val term: ClassTerm,

    @Column(name = "termId")
    open val termIdentifier: String = String.empty,

    @Column(name = "url")
    open val url: String,

    @Column(name = "number")
    open val number: Long,

    @Column(name = "subjectAndIdentifier", nullable = true)
    open val subjectAndIdentifier: String,

    @Column(name = "roleId", nullable = true)
    open var roleId: Long = 0,

    @Column(name = "channelId", nullable = true)
    open var channelId: Long = 0,

    @Column(name = "guildId", nullable = false)
    open val guildId: Long,

    @Column(name = "autoFilled")
    /**
     * A class that was auto populated via api
     */
    open val autoFilled: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "school_id")
    open val school: School,

    @Id
    override val id: UUID = UUID.nameUUIDFromBytes((name+termIdentifier+guildId+number).toByteArray())

    ) : Pagable, Identifiable

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

        /*
        field {
            name = "Professors"
            value = professors.joinToString { "`${it.firstName}, ${it.lastName}`" }
        }

        field {
            name = "Assignment Count"
            value = assignments.size.toString()
        }

         */
    }


     override fun getAsEmbed(guild: Guild): MessageEmbed = Embed {
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

         /*
        field {
            name = "Professors"
            value = professors.joinToString { "`${it.firstName}, ${it.lastName}`" }
        }

        field {
            name = "Assignment Count"
            value = assignments.size.toString()
            inline = true
        }

          */

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




