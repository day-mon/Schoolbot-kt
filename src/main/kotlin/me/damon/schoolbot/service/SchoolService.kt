package me.damon.schoolbot.service

import dev.minn.jda.ktx.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.School
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.*

@Service("SchoolService")
open class SchoolService
{
    @Autowired
    private lateinit var schoolRepo: SchoolRepository

    @Throws(IllegalArgumentException::class)
    open suspend fun saveSchool(school: School, commandEvent: CommandEvent): School
    {
        val guild = commandEvent.guild
        val regex = Regex("\\s+")
        /*
        val role = guild.createRole()
            .setColor(Random().nextInt(0xFFFFF))
            .setName(school.name.replace(regex, "-"))
            .await()

         */

        school.apply {
            roleId = 0L
            guildId = guild.idLong
        }

       return withContext(Dispatchers.IO) {
           schoolRepo.save(school)
       }
    }

    @Cacheable(cacheNames = ["schoolsByGuild"], key ="#guildId")
    open fun getSchoolsByGuildId(guildId: Long) = schoolRepo.querySchoolsByGuildId(guildId)

    @CacheEvict(cacheNames = ["schoolsByGuild"], key = "#school.guildId")
    open fun deleteSchool(school: School) = schoolRepo.delete(school)


}