package me.damon.schoolbot.ext

import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.command.CommandChoice
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun LocalDateTime.formatDate(): String = this.format(DateTimeFormatter.ofPattern("M/dd/yyyy @ hh:mm a", Constants.DEFAULT_LOCALE))
fun BigDecimal.parseNumbersWithCommas() = DecimalFormat("#,###.00").format(this).toString()
fun String.toUUID(): UUID? =  try {  UUID.fromString(this) } catch (e: Exception) { null }
fun String.asCommandChoice() = CommandChoice(this, this)

fun String.toTitleCase(): String {
    // lol something is wrong with me
    val builder = StringBuffer()
    val strSplit = this.split(Constants.SPACE_REGEX).filter { it.isNotBlank() }

    strSplit.forEachIndexed { s, _ ->
        val string = strSplit[s]
        val stringCharArray = string.toCharArray()
        stringCharArray.forEachIndexed { index, _ ->
            if (index == 0) builder.append(stringCharArray[index].uppercaseChar())
            else builder.append(stringCharArray[index].lowercaseChar())
        }
        if (s != strSplit.size - 1 && strSplit.size > 1) builder.append(" ")
    }
    return builder.toString()

}

val String.Companion.empty: String
    get() { return "" }

