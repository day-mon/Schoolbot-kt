package me.damon.schoolbot.commands.main.dev

import me.damon.schoolbot.ext.tryDelete
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import java.io.File

class ClearErrorLog : Command(
    name = "clearerrorlog",
    description = "Clears the error log",
    category = CommandCategory.DEV,
    commandPrerequisites = "Must be a developer"
    )
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val file = File("schoolbot-error.log")

        if (file.exists().not()) return run { event.replyErrorEmbed("There is no error log as of now.") }
        if (file.readLines().isEmpty()) return run { event.replyErrorEmbed("Log file is already empty") }

        val deleted = file.tryDelete()

        if (!deleted) return run { event.replyErrorEmbed("Error while trying to delete log. ")}
    }
}