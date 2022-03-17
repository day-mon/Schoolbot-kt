package me.damon.schoolbot.ext

import me.damon.schoolbot.objects.command.CommandChoice
import java.math.BigDecimal
import java.security.MessageDigest
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.formatDate(): String = this.format(DateTimeFormatter.ofPattern("M/dd/yyyy"))
fun BigDecimal.parseNumbersWithCommas() = DecimalFormat("#,###.00").format(this).toString()
fun String.asHyperText(url: String) = "[${this}](${url})"
fun String.asCommandChoice() = CommandChoice(this, this)
fun String.hash(): String {
    val HEX_CHARS = "0123456789ABCDEF"
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }
    return result.toString()
}
val String.Companion.empty: String
    get() { return "" }

