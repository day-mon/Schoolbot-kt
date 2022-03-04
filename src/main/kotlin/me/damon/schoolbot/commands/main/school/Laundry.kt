package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.LaundryRemind
import me.damon.schoolbot.commands.sub.school.LaundryReminderCancel
import me.damon.schoolbot.commands.sub.school.LaundryView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory

class Laundry : Command(
    name = "Laundry",
    category = CommandCategory.SCHOOL,
    children = listOf(
        LaundryRemind(),
        LaundryReminderCancel(),
        LaundryView()
    ),
    description = "Displays laundry availability in a given dormitory",
)