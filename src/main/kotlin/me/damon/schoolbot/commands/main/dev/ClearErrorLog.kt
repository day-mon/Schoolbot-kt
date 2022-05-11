package me.damon.schoolbot.commands.main.dev

import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.ext.printWriter
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

        if (file.exists().not()) return  event.replyErrorEmbed("There is no error log as of now.")
        if (file.readLines().isEmpty()) return event.replyErrorEmbed("Log file is already empty")


        file.printWriter().use {
            it.write(String.empty)
        }

        event.replyMessage("Error log has been cleared")
    }
}