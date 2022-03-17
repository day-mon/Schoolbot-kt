package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.laundry.LaundryReminderAdd
import me.damon.schoolbot.commands.sub.school.laundry.LaundryReminderCancel
import me.damon.schoolbot.commands.sub.school.laundry.LaundryView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory

class Laundry : Command(
    name = "Laundry",
    category = CommandCategory.SCHOOL,
    children = listOf(
        LaundryView()
    ),
    group = mapOf("reminder" to listOf(
        LaundryReminderAdd(),
        LaundryReminderCancel(),
    )),
    description = "Displays laundry availability in a given dormitory",
)