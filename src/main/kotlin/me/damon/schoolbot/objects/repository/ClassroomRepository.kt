package me.damon.schoolbot.objects.repository;

import me.damon.schoolbot.objects.school.Classroom
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ClassroomRepository : JpaRepository<Classroom, UUID>
{}