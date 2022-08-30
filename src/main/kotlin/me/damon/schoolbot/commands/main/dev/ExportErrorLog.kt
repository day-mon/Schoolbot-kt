package me.damon.schoolbot.commands.main.dev

import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.FileUpload
import org.springframework.stereotype.Component
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.fileSize

@Component
class ExportErrorLog : Command(
    name = "ExportErrorLog",
    description = "Exports the error log",
    category = CommandCategory.DEV,
    commandPrerequisites = "Must be a developer",

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val file = File("schoolbot-error.log")

        if (file.exists().not()) return event.replyMessage("There is no error log file as of now.")
        if (file.readLines().isEmpty()) return event.replyMessage("Log file is empty as of now.")

        // blocking but /shrug
        val size = Path(file.path).fileSize()
        if (size > event.jda.selfUser.allowedFileSize) return event.replyMessage("Log file is large to export to discord")

        val fileUpload: FileUpload = FileUpload.fromData(file)


        event.user.openPrivateChannel().queue {
            it.sendMessage("Log file as of ${Constants.CURRENT_TIME}. It currently has a size of `$size` bytes")
                .addFiles(fileUpload)
                .queue({
                       event.replyMessage("Log sent in PMs.")
                },
                    ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER)
                    { event.replyMessage("I cannot send a PM to you.." ) }
                )
        }
    }
}