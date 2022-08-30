package me.damon.schoolbot.ext

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.Constants
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
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

fun <K, V> MutableMap<K, V>.put(pair: Pair<K, V>) = this.put(pair.first, pair.second)
fun <K, V> MutableMap<K, V>.putAll(vararg pairs: Pair<K, V>) = this.putAll(pairs)
inline fun <reified T: Enum<T>> emptyEnumSet(): EnumSet<T> = EnumSet.noneOf(T::class.java)
inline fun <reified T: Enum<T>> enumSetOf(vararg elements: T): EnumSet<T> = EnumSet.copyOf(elements.toList())
inline fun <reified T: Enum<T>> enumSetOf(collection: Collection<T>): EnumSet<T> = EnumSet.copyOf(collection)
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

fun InteractionHook.replyErrorEmbed(mainTitle: String = "Error has occurred", body: String, actionRows: List<Button> = emptyList(), content: String = String.empty) = this.editOriginalEmbeds(
    Embed {
        title = mainTitle
        description = body
        color = Constants.RED
    }
).setActionRow(actionRows)
    .setContent(content)
    .queue()
fun File.printWriter(append: Boolean = false ): PrintWriter =
PrintWriter(FileOutputStream(this.path, append))