package me.damon.schoolbot.ext

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.Constants
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import java.io.*
import java.util.*

fun InputStream.string(): String
{
    val reader = BufferedReader(this.reader())
    val content = StringBuilder()
    reader.use { read ->
        var line = read.readLine()
        while (line != null) {
            content.appendLine(line)
            line = read.readLine()
        }
    }
    return content.toString()
}

fun File.tryDelete(): Boolean
{
    val logger by SLF4J
    return try
    {
        this.delete()
    }
    catch (e: SecurityException)
    {
        logger.error("Error occurred while trying to delete ${this.name}", e)
        false
    }
}

fun InteractionHook.replyErrorEmbed(mainTitle: String = "Error has occurred", body: String, actionRows: List<ActionRow> = Collections.emptyList(), content: String = String.empty) = this.editOriginalEmbeds(
    Embed {
        title = mainTitle
        description = body
        color = Constants.RED
    }
).setActionRows(actionRows)
    .setContent(content)
    .queue()
fun File.printWriter(append: Boolean = false ): PrintWriter =
PrintWriter(FileOutputStream(this.path, append))