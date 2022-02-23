package me.damon.schoolbot.commands.main.dev

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlin.io.path.Path
import kotlin.io.path.fileSize

class ExportErrorLog : Command(
    name = "ExportErrorLog",
    description = "Exports the error log",
    category = CommandCategory.DEV,
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val file = File("schoolbot-error.log")

        if (file.exists().not())
        {
            event.replyMessage("There is no error log file as of now.")
            return
        }


        if (file.readLines().isEmpty())
        {
            event.replyMessage("Log file is empty as of now.")
            return
        }

        // add

        // blocking but /shrug
        if (Path(file.path).fileSize() > event.jda.selfUser.allowedFileSize)
        {
            event.replyMessage("Log file is large to export to discord")
            return
        }

        event.user.openPrivateChannel().queue {
            it.sendMessage("Log file as of ${Instant.now()}")
                .addFile(file)
                .queue({
                       event.replyMessage("Log sent in PMs.")
                },
                    ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER)
                    { event.replyMessage("I cannot send a PM to you.." ) }
                )
        }
    }
}