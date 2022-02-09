package me.damon.schoolbot.commands.main.`fun`

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent

class Ask : Command(
    name = "Ask",
    description = "Dont ask to ask",
    category = CommandCategory.FUN

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        event.replyMessage(
         "Refer to: https://dontasktoask.com/"
        )
    }
}