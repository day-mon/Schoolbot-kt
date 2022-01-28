package me.damon.schoolbot.objects.repository;

import me.damon.schoolbot.objects.school.Assignment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AssignmentRepository : JpaRepository<Assignment, UUID>
