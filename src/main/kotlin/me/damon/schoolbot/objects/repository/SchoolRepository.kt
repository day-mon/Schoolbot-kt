package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SchoolRepository : JpaRepository<School, UUID>
