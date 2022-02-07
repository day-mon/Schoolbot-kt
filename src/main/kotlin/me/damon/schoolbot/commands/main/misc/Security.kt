package me.damon.schoolbot.commands.main.misc

import me.damon.schoolbot.commands.sub.misc.SecurityPrice
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class Security : Command (
    name = "Security",
    description = "Allows you to see the price or quote of a given security",
    category = CommandCategory.MISC,
    children = listOf(
        SecurityPrice()
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent) {}
}