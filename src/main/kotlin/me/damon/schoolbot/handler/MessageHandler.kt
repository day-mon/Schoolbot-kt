package me.damon.schoolbot.handler

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.SLF4J
import dev.minn.jda.ktx.messages.edit
import kotlinx.coroutines.*
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.ext.await
import me.damon.schoolbot.ext.string
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.concurrent.thread

private val FILE_EXTENSIONS = listOf(
    "txt", "java", "cpp", "xml", "csharp", "asm", "js", "php", "r", "py", "go", "python", "ts", "html", "css", "scss"
)
private val pool = Executors.newScheduledThreadPool(5) {
    thread(start = false, name = "Schoolbot Upload-Thread", isDaemon = true, block = it::run)
}
// wont cancel the scope if jobs fail
private val supervisor = SupervisorJob()
private val context =  CoroutineScope(pool.asCoroutineDispatcher() + supervisor)



class MessageHandler
{
    private val logger by SLF4J

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

                    context.launch {
                        doUpload(it, event)
                    }
                }
            }
    }

    private suspend fun doUpload(triple: Triple<CompletableFuture<Message>, CompletableFuture<InputStream>, CompletableFuture<Void>>, event: MessageReceivedEvent)
    {
        val client = event.jda.httpClient
        val message = triple.first.getNow(null) ?: return run {
            event.channel.sendMessage("Upload Failed. Reason: **Message is not available to edit**").queue()
        }
        val stream = triple.second.getNow(null) ?: return run {
            message.editMessage("Upload Failed. Reason: **Input stream is not available to upload**").queue()
        }


        val request =  Request.Builder().url("https://pastecord.com/documents")
            .addHeader("User-Agent", "School bot (https://github.com/tykoooo/School-Bot-Remastered)")
            .post(
                RequestBody.create(
                    MediaType.parse("application/json"),
                    stream.string()
                )
            ).build()

        withContext(Dispatchers.IO) {
            stream.close()
        }

        client.newCall(request).await(scope = context) { response ->
             when
             {
                 response.isSuccessful ->
                 {

                     val body = response.body()?.string() ?: return@await run {
                         logger.error("Response body is null")
                         message.editMessage("Upload Failed. Reason: **Response body is null**").queue()
                     }

                     val responseJson = DataObject.fromJson(body)

                     if (!responseJson.hasKey("key")) return@await run {
                         logger.error("Body is either malformed or body responded with an unexpected response \n Body: {}", body)
                         message.editMessage("Body returned unexpected response").queue()
                     }

                     val pastecordEnding = responseJson.get("key")

                     val urlToSend = "https://pastecord.com/${pastecordEnding}"
                     message.editMessage("Successfully uploaded ${event.author.asMention}'s message [$urlToSend]")
                         .queue()
                     event.message.delete().queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))

                 }

                 response.code() > 500 ->
                 {
                     logger.error("Internal server returned error code: {}", response.code(), response.asException())
                     sendErrorEmbed(message, response.asException())
                 }

                 else ->
                 {
                     logger.error("Strange error has occurred", response.asException())
                     sendErrorEmbed(message, response.asException())
                 }

             }
        }
    }

    private fun sendErrorEmbed(message: Message, e: Exception)
    {
       message.edit("",
                Embed {
                    title = "Error occurred. Send this message to a developer if it constantly occurs"
                    field {
                        title = "Cause"
                        value = e.cause.toString()
                        inline = true
                    }
                    description = """```kt
                            ${e.stackTraceToString()}
                        ```""".trimIndent()
                }).queue()
    }

}