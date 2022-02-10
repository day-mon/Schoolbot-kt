package me.damon.schoolbot.commands.main.school

import me.damon.schoolbot.commands.sub.school.LaundryRemind
import me.damon.schoolbot.commands.sub.school.LaundryView
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent

class Laundry : Command(
    name = "Laundry",
    category = CommandCategory.SCHOOL,
    children = listOf(
        LaundryRemind(),
        LaundryView()
    ),
    description = "Displays laundry availability in a given dormitory",
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent){}
}