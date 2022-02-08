package me.damon.schoolbot.commands.main.misc

import me.damon.schoolbot.commands.sub.misc.SecurityPrice
import me.damon.schoolbot.commands.sub.misc.SecurityQuote
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent

class Security : Command (
    name = "Security",
    description = "Allows you to see the price or quote of a given security",
    category = CommandCategory.MISC,
    children = listOf(
        SecurityPrice(),
        SecurityQuote()
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent) {}
}