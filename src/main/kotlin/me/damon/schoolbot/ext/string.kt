package me.damon.schoolbot.ext

import me.damon.schoolbot.objects.command.CommandChoice
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.formatDate(): String = this.format(DateTimeFormatter.ofPattern("M/dd/yyyy"))
fun BigDecimal.parseNumbersWithCommas() = DecimalFormat("#,###.00").format(this).toString()
fun String.asHyperText(url: String) = "[${this}](${url})"
fun String.asCommandChoice() = CommandChoice(this, this)
