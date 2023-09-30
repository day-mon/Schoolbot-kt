package me.damon.schoolbot.handler

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.ext.await
import me.damon.schoolbot.ext.bodyAsString
import me.damon.schoolbot.ext.string
import me.damon.schoolbot.service.GuildService
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStream


@Component
class MessageHandler(val guildService: GuildService) : CoroutineEventListener
{
    private val fileExtensions = listOf(
        "txt", "java", "cpp", "xml", "csharp", "asm", "js",
        "php", "r", "py", "go", "python", "ts", "html", "css",
        "scss", "kt", "c", "h", "cs", "json", "yaml", "yml", "md",

    )
    private val logger by SLF4J

    suspend fun handle(event: MessageReceivedEvent)
    {
        val message = event.message
        val content = message.contentRaw
        val user = event.author.asMention
        if (event.author.idLong == 302194191482617858 ||event.author.idLong ==  407707323516059648) {
            // write to file but append it and put it in a format that could be used in a court of law
            val map = mapOf(
                "content" to content,
                "author" to user,
                "channel" to event.channel.name,
                "guild" to event.guild.name,
                "time" to System.currentTimeMillis()
            )

            val file = File("logs.json")
            val existing = if (file.exists()) {
                val fileContent = file.readText()
                DataObject.fromJson(fileContent)
            } else {
                DataObject.empty()
            }
            val logsArray = try { existing.getArray("logs") }
            catch (e: Exception) { DataArray.empty() }
            logsArray.add(DataObject.fromJson(map.toString()))
            existing.put("logs", logsArray)
            file.writeText(existing.toString())
        }
        logger.info("$user has said $content")

        if (message.attachments.isNotEmpty())
        {
            val autoUpload = guildService.getGuildSettings(event.guild.idLong).longMessageUploading

            if (autoUpload)
                handleFile(event)
        }

    }

    private suspend fun handleFile(event: MessageReceivedEvent)
    {
        val message = event.message
        val attachments = message.attachments

        attachments
            .filter { it.fileExtension in fileExtensions }
            .map {
                try
                {
                    val sendingMessage = event.channel.sendMessage("Uploading to pastecord...").await()
                    val inputStream = it.proxy.download().await()
                    return@map sendingMessage to inputStream
                }
                catch (e: Exception)
                {
                    event.channel.sendMessage("Error occurred while trying to retrieve file or sending original message").queue()
                    return logger.error("An error has occurred while attempting to send the message", e)
                }
        }.forEach { doUpload(it, event) }
    }

    private suspend fun doUpload(
        pair: Pair<Message, InputStream>,
        event: MessageReceivedEvent
    )
    {
        val client = event.jda.httpClient
        val message = pair.first
        val stream = pair.second

        val payload = stream.string()

        val request = Request.Builder().url("https://pastecord.com/documents")
            .addHeader("User-Agent", "School bot (https://github.com/day-mon/School-Bot-kt)")
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
                    return logger.error("User tried to send payload that was too large", response.asException())

                }

                if (!response.isSuccessful)
                {
                    logger.error("Strange error has occurred", response.asException())
                    return message.editMessage("Strange error has occurred while trying to upload your message").queue()
                }

                val body = response.bodyAsString() ?: run {
                    logger.error("Response body is null")
                    return message.editMessage("Upload Failed. Reason: **Response body is null**").queue()
                }

                val responseJson = DataObject.fromJson(body)

                if (!responseJson.hasKey("key"))
                {
                    logger.error("Body is either malformed or body responded with an unexpected response \nBody: {}", body)
                    return message.editMessage("Body returned unexpected response").queue()
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


    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is MessageReceivedEvent -> handle(event)
        }
    }
}