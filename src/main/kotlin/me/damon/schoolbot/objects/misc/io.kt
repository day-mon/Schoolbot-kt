package me.damon.schoolbot.objects.misc

import java.io.BufferedReader
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