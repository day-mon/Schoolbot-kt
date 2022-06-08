package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.laundry.LaundryReminderAdd
import me.damon.schoolbot.commands.sub.school.laundry.LaundryReminderCancel
import me.damon.schoolbot.commands.sub.school.laundry.LaundryView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import org.springframework.stereotype.Component

@Component
class Laundry(
    laundryView: LaundryView,
    laundryReminderAdd: LaundryReminderAdd,
    laundryReminderCancel: LaundryReminderCancel
) : Command(
    name = "Laundry",
    category = CommandCategory.SCHOOL,
    children = listOf(
        laundryView
    ),
    group = mapOf("reminder" to listOf(
       laundryReminderAdd,
        laundryReminderCancel
    )),
    description = "Displays laundry availability in a given dormitory",
)