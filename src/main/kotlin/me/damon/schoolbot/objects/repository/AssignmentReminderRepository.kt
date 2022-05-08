package me.damon.schoolbot.objects.repository;

import me.damon.schoolbot.objects.school.AssignmentReminder
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AssignmentReminderRepository : JpaRepository<AssignmentReminder, UUID>