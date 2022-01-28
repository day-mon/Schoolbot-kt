package me.damon.schoolbot.objects.repository;

import me.damon.schoolbot.objects.school.Professor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProfessorRepository : JpaRepository<Professor, UUID>
