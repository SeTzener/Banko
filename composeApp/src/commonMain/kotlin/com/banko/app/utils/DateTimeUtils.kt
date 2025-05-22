package com.banko.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime


fun beginningOfCurrentMonth(): LocalDateTime {
    val currentInstant = Clock.System.now()
    val currentTZ = TimeZone.currentSystemDefault()
    val currentDate = currentInstant.toLocalDateTime(currentTZ).date

    return LocalDateTime(currentDate.year, currentDate.month, 1, 0, 0)
}

val LocalDateTime.Companion.now: LocalDateTime
    get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun getLastDayOfMonth(year: Int, month: Int): Int {
    val firstDayOfThisMonth = LocalDate(year, month, 1)
    val firstDayOfNextMonth = firstDayOfThisMonth.plus(DatePeriod(months = 1))
    val lastDayOfThisMonth = firstDayOfNextMonth.minus(DatePeriod(days = 1))
    return lastDayOfThisMonth.dayOfMonth
}