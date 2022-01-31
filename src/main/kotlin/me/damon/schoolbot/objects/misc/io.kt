package me.damon.schoolbot.objects.misc

import java.io.BufferedReader
import java.io.InputStream

fun InputStream.string(): String
{
    val reader = BufferedReader(this.reader())
    val content = StringBuilder()
    reader.use { reader ->
        var line = reader.readLine()
        while (line != null) {
            content.appendLine(line)
            line = reader.readLine()
        }
    }
    return content.toString()
}