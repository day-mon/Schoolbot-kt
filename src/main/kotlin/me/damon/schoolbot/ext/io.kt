package me.damon.schoolbot.ext

import dev.minn.jda.ktx.SLF4J
import java.io.BufferedReader
import java.io.File
import java.io.InputStream

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
    } catch (e: SecurityException)
    {
        logger.error("Error occurred while trying to delete ${this.name}", e)
        false
    }

}
