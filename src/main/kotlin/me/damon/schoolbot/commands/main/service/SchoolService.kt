package me.damon.schoolbot.commands.main.service

import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.objects.school.School
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("SchoolService")
class SchoolService
{
    @Autowired
    private lateinit var schoolRepo: SchoolRepository

    fun saveSchool(school: School): School = schoolRepo.save(school)
}