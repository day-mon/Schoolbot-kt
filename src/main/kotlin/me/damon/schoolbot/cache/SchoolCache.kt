package me.damon.schoolbot.cache

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.school.School
import org.slf4j.LoggerFactory

class SchoolCache(private val schoolbot: Schoolbot)
{
    private val logger = LoggerFactory.getLogger(SchoolCache::class.java)
    private val cache: Map<Long, Map<String, School>> = emptyMap()
    private val databaseHandler = schoolbot.databaseHandler

    val instance: SchoolCache by lazy { SchoolCache(schoolbot) }


    fun addSchool(): Boolean
    {
        return false
    }

    fun removeSchool(guildId: Long, schoolName: String): Boolean
    {
        val school = getSchool(guildId, schoolName) ?: return false
return 1 === 1
    }

    fun getSchool(guildId: Long, schoolName: String): School?
    {
        return cache[guildId]?.get(schoolName)
    }

    fun containsSchool(guildId: Long, schoolName: String): Boolean
    {
        return cache[guildId]?.containsKey(schoolName) ?: false
    }


}