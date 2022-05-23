package me.damon.schoolbot.objects.school

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "courses")
@Entity(name = "courses")
class Course(
    @Column(name = "name", nullable = false)
     var name: String,

    @Column(name = "description", columnDefinition = "text")
     var description: String,

    @Column(name = "topic", nullable = true)
     val topic: String?,

    @Column(name = "prerequisite")
     val prerequisite: String?,

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
     val reminders: List<CourseReminder> = listOf(),

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
     val professors: List<Professor>,

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
     val assignments: List<Assignment>,

    @Column(name = "startDate", nullable = false)
     var startDate: Instant,

    @Column(name = "endDate", nullable = false)
     var endDate: Instant,

    @Column(name = "meeting_days", nullable = false)
     var meetingDays: String, // days comma delimited

    @Column(name = "termId")
     val termIdentifier: String = String.empty,

    @Column(name = "url")
     var url: String,

    @Column(name = "number")
     var number: Long,

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
     * A class that was autopopulated via api
     */
     val autoFilled: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "school")
     val school: School,

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

        if (prerequisite != null)
        {
            field {
                name = "Prerequisites"
                value = prerequisite
            }
        }

        if (topic != null) {
            field {
                name = "Topic"
                value = topic
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
        if (this@Course.startDate.atZone(school.zone).year == 1970) footer("If you're seeing this the reason the start/end date is 1970 is that the course had no posted start/end date")
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
                 value = prerequisite
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


         field {
             name = "Channel"
             value = channel?.asMention ?: "N/A"
         }

         if (topic != null) {
             field {
                 name = "Topic"
                 value = topic
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
         if (this@Course.startDate.atZone(school.zone).year == 1970) footer("If you're seeing this the reason the start/end date is 1970 is that the course had no posted start/end date")


     }
}




