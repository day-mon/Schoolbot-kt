package me.damon.schoolbot.service

import dev.minn.jda.ktx.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.School
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service("SchoolService")
class SchoolService
{
    @Autowired
    private lateinit var schoolRepo: SchoolRepository

    @Throws(IllegalArgumentException::class)
    suspend fun saveSchool(school: School, commandEvent: CommandEvent): School
    {
        val guild = commandEvent.guild
        val regex = Regex("\\s+")
        val role = guild.createRole()
            .setColor(Random().nextInt(0xFFFFF))
            .setName(school.name.replace(regex, "-"))
            .await()

        school.roleId = role.idLong
        school.guildId = guild.idLong

       return withContext(Dispatchers.IO) {
           schoolRepo.save(school)
       }
    }
}