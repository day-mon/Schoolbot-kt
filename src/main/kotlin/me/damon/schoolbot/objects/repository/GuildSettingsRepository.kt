package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.guild.GuildSettings
import org.springframework.data.jpa.repository.JpaRepository

interface GuildSettingsRepository : JpaRepository<GuildSettings, Long>