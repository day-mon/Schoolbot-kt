package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.School
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SchoolRepository : JpaRepository<School, UUID>
