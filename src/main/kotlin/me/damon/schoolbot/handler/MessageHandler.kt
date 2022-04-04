package me.damon.schoolbot.handler

import dev.minn.jda.ktx.SLF4J
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.ext.await
import me.damon.schoolbot.ext.bodyAsString
import me.damon.schoolbot.ext.string
import me.damon.schoolbot.service.GuildService
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.concurrent.CompletableFuture

private val FILE_EXTENSIONS = listOf(
    "txt", "java", "cpp", "xml", "csharp", "asm", "js", "php", "r", "py", "go", "python", "ts", "html", "css", "scss"
)
private val supervisor = SupervisorJob()
private val context = CoroutineScope(Dispatchers.IO + supervisor)
@Component
class MessageHandler(val service: GuildService)
{

    private val logger by SLF4J

    fun handle(event: MessageReceivedEvent)
    {
        val message = event.message

        if (message.attachments.isNotEmpty())
        {
            val autoUpload = service.getGuildSettings(event.guild.idLong).longMessageUploading
            if (autoUpload)
            {
                handleFile(event)
            }
        }

    }

    private fun handleFile(event: MessageReceivedEvent)
    {
        val message = event.message
        val attachments = message.attachments

        attachments.stream().filter { it.fileExtension in FILE_EXTENSIONS }.map {
            // could probably use await
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

                context.launch { doUpload(it, event) }
            }
        }
    }

    private suspend fun doUpload(
        triple: Triple<CompletableFuture<Message>, CompletableFuture<InputStream>, CompletableFuture<Void>>,
        event: MessageReceivedEvent
    )
    {
        val client = event.jda.httpClient
        val message = triple.first.getNow(null) ?: return run {
            event.channel.sendMessage("Upload Failed. Reason: **Message is not available to edit**").queue()
        }
        val stream = triple.second.getNow(null) ?: return run {
            message.editMessage("Upload Failed. Reason: **Input stream is not available to upload**").queue()
        }

        val payload = stream.string()

        val request = Request.Builder().url("https://pastecord.com/documents")
            .addHeader("User-Agent", "School bot (https://github.com/tykoooo/School-Bot-kt)")
            .post(
                RequestBody.create(
                    MediaType.parse("application/json"),
                    payload
                )
            ).build()


        try
        {
            client.newCall(request).await().use { response ->

                
                if (response.code() == 413)
                {
                    message.editMessage("Your payload is too large ").queue()
                    event.message.delete().queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))
                    logger.error("User tried to send payload that was too large", response.asException())
                    return
                }

                if (!response.isSuccessful)
                {
                    logger.error("Strange error has occurred", response.asException())
                    message.editMessage("Strange error has occurred while trying to upload your message").queue()
                    return
                }

                val body = response.bodyAsString() ?: return run {
                    logger.error("Response body is null")
                    message.editMessage("Upload Failed. Reason: **Response body is null**").queue()
                }

                val responseJson = DataObject.fromJson(body)

                if (!responseJson.hasKey("key")) return run {
                    logger.error(
                        "Body is either malformed or body responded with an unexpected response \n Body: {}", body
                    )
                    message.editMessage("Body returned unexpected response").queue()
                }

                val pastecordEnding = responseJson.get("key")

                val urlToSend = "https://pastecord.com/${pastecordEnding}"
                message.editMessage("Successfully uploaded ${event.author.asMention}'s message [$urlToSend]").queue()
                event.message.delete().queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))
            }
        }
        catch (e: Exception)
        {
            message.editMessage("Error has occurred while trying to upload this to pastecord").queue()
            logger.error("Error has occurred", e)
        }
    }
}