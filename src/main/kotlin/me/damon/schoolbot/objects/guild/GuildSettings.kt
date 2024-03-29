package me.damon.schoolbot.objects.guild

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "guildSettings")
@Entity(name = "GuildSettings")
class GuildSettings(
    @Id
    @Column(name = "guildId", updatable = false)
    val guildId: Long,

    @Column(name = "longMessageUploading")
    var longMessageUploading: Boolean = true,

    @Column(name = "deleteRemindableEntityOnLastReminder")
    var deleteRemindableEntityOnLastReminder: Boolean = true
)