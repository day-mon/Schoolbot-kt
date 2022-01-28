package me.damon.schoolbot.handler

import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.concurrent.thread

private val FILE_EXTENSIONS = listOf(
    "txt", "java", "cpp", "xml", "csharp", "asm", "js", "php", "r", "py", "go", "python", "ts", "html", "css", "scss"
)
private val logger = LoggerFactory.getLogger(MessageHandler::class.java)
private val pool = Executors.newScheduledThreadPool(5) {
    thread(start = false, name = "Schoolbot Upload-Thread", isDaemon = true, block = it::run)
}

private val client = OkHttpClient()


class MessageHandler(schoolbot: Schoolbot)
{
    fun handle(event: MessageReceivedEvent)
    {
        val message = event.message

        if (message.attachments.isNotEmpty())
        {
            // guild settings check here
            handleFile(event)
        }
    }

    private fun handleFile(event: MessageReceivedEvent)
    {
        val message = event.message
        val attachments = message.attachments


        attachments.stream().filter { it.fileExtension in FILE_EXTENSIONS }
            .map {
                val messageFuture = event.channel.sendMessage("Uploading to pastecord...").submit()
                val inputStreamFuture = it.retrieveInputStream()
                val allFutures = CompletableFuture.allOf(messageFuture, inputStreamFuture)
                return@map Triple(messageFuture, inputStreamFuture, allFutures)
            }.forEach {
                it.third.whenCompleteAsync { _, throwable ->

                    if (throwable != null)
                    {
                        logger.error("An error has occurred while attempting to send the message", throwable)
                        return@whenCompleteAsync
                    }

                    val sentMessage = it.first.getNow(null) ?: throw IllegalStateException("Message is not present")
                    val inputStream =
                        it.second.getNow(null) ?: throw IllegalStateException("Inputstream was not present")
                    val urlToSend = "https://pastecord.com/${doUpload(inputStream)}"
                    sentMessage.editMessage("Successfully uploaded ${event.author.asTag}'s message [$urlToSend]")
                        .queue()
                }
            }
    }

    private fun doUpload(stream: InputStream)
    {
        
        val request = Request.Builder().url("https://pastecord.com/documents")
            .addHeader("User-Agent", "School bot (https://github.com/tykoooo/School-Bot-Remastered)").post(
                RequestBody.create(
                    MediaType.parse("application/json"),
                    ""
                )
            ).build()

    }
}