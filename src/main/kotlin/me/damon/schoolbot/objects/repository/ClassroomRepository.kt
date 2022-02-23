package me.damon.schoolbot.objects.repository

import me.damon.schoolbot.objects.school.Course
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ClassroomRepository : JpaRepository<Course, UUID>