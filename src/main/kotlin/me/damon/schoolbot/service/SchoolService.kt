package me.damon.schoolbot.service

import dev.minn.jda.ktx.SLF4J
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.repository.ClassroomRepository
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.School
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("SchoolService")
open class SchoolService
{
    @Autowired
    private lateinit var schoolRepo: SchoolRepository

    @Autowired
    private lateinit var classroomRepo: ClassroomRepository

    private val logger by SLF4J

    @Throws(IllegalArgumentException::class)
   // @Cacheable
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

   // @Cacheable(cacheNames = ["schoolsByGuild"], unless = "s", key="#result" )
    open fun getSchoolsByGuildId(guildId: Long) = schoolRepo.querySchoolsByGuildId(guildId)

    open fun getPittSchoolsInGuild(guildId: Long) = schoolRepo.findSchoolsByIsPittSchoolAndGuildId(guildId = guildId)

    open fun findSchoolInGuild(guildId: Long, name: String) = schoolRepo.findSchoolByNameAndGuildId(name, guildId)

   // @CacheEvict(cacheNames = ["schoolsByGuild"], key = "#school.guildId")
    open fun deleteSchool(school: School, event: CommandEvent)
    {
        schoolRepo.delete(school)
        event.jda.getRoleById(school.roleId)?.delete()?.queue( null) {
            logger.error("An error has occurred while trying to delete role for {}", school.name, it)
        } ?: return

    }
}