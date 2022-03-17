package me.damon.schoolbot.service

import me.damon.schoolbot.objects.guild.GuildSettings
import me.damon.schoolbot.objects.repository.GuildSettingsRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service("GuildService")
open class GuildService(
    private val guildRepository: GuildSettingsRepository
)
{
    @Cacheable(cacheNames = ["guild_settings"], key = "#guildId")
    open fun getGuildSettings(guildId: Long): GuildSettings
    {
        val exist = guildRepository.existsById(guildId)
        return if (exist)
        {
            guildRepository.getById(guildId)
        }
        else
        {
            guildRepository.save(
                GuildSettings(guildId = guildId)
            )
        }
    }

    open fun createSettings(settings: GuildSettings): GuildSettings = guildRepository.save(settings)

    @CacheEvict(cacheNames = ["guild_settings"], key = "#guildId")
    open fun removeGuildInstance(guildId: Long) = guildRepository.deleteById(guildId)
}