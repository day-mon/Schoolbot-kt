package me.damon.schoolbot.commands.main.`fun`

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import org.springframework.stereotype.Component

@Component
class Tias : Command(
    name = "Tias",
    description = "Try it and see",
    category = CommandCategory.FUN
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        event.replyMessage(
            "https://tryitands.ee/"
        )
    }
}