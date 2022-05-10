package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "courses")
@Entity(name = "courses")
@Transactional
open class Course(
    @Column(name = "name", nullable = false)
    open val name: String,

    @Column(name = "description", columnDefinition = "text")
    open val description: String,

    @Column(name = "topic", nullable = true)
    open val topic: String?,

    @Column(name = "prerequisite")
    open val prerequisite: String?,

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    open val reminders: MutableList<CourseReminder> = mutableListOf(),

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    open val professors: List<Professor>,

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    open val assignments: List<Assignment>,

    @Column(name = "startDate", nullable = false)
    open val startDate: Instant,

    @Column(name = "endDate", nullable = false)
    open val endDate: Instant,

    @Column(name = "meeting_days", nullable = false)
    open val meetingDays: String, // days comma delimited

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
     * A class that was autopopulated via api
     */
    open val autoFilled: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "school")
    open val school: School,

    @Id
    override val id: UUID = UUID.nameUUIDFromBytes((name+termIdentifier+guildId+number).toByteArray())

    ) : Pagable, Identifiable

{
    @Async
    override fun getAsEmbed(): MessageEmbed = Embed {
        title = "$name | $subjectAndIdentifier"
        url = this@Course.url

        field {
            name = "Description"
            value = this@Course.description
        }

        if (prerequisite != null)
        {
            field {
                name = "Prerequisites"
                value = prerequisite!!
            }
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

         if (prerequisite != null)
         {
             field {
                 name = "Prerequisites"
                 value = prerequisite!!
             }
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

         if (topic != null) {
             field {
                 name = "Topic"
                 value = topic!!
             }
         }

         field {
             name = "Role"
             value = role?.asMention ?: "N/A"
         }

         field {
             name = "Start Date & Time"
             println(startDate.epochSecond)
             value = "<t:${startDate.epochSecond}>"

         }

         field {
             name = "End Date & Time"
             value = "<t:${endDate.epochSecond}>"
         }

         color = role?.colorRaw ?: 0xFFFF

    }
}




