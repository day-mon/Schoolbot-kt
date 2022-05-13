package me.damon.schoolbot.service

import me.damon.schoolbot.objects.guild.GuildSettings
import me.damon.schoolbot.objects.repository.GuildSettingsRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service("GuildService")
 class GuildService(
    private val guildRepository: GuildSettingsRepository
) : SpringService
{
    fun getGuildSettings(guildId: Long): GuildSettings
    {
        val exist = guildRepository.existsById(guildId)
        return if (exist)
        {
            guildRepository.getById(guildId)
        }
        else
        {
            guildRepository.save(GuildSettings(guildId = guildId ))
        }
    }

    fun createSettings(settings: GuildSettings): GuildSettings? = guildRepository.save(settings)


    fun removeGuildInstance(guildId: Long) = guildRepository.deleteById(guildId)
}