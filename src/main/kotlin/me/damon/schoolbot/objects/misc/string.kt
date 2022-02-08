package me.damon.schoolbot.objects.misc

import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.formatDate(): String = this.format(DateTimeFormatter.ofPattern("M/dd/yyyy"))
fun BigDecimal.parseNumbersWithCommas() = DecimalFormat("#,###.00").format(this).toString()