package com.banko.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime


/**
 * Parses an API timestamp. Accepts both ISO-8601 instants
 * (`2026-11-16T12:34:56Z`) and zone-less values, which are interpreted as UTC.
 * Returns null for missing or malformed values instead of crashing the UI.
 */
internal fun parseApiInstant(value: String?): Long? {
    val raw = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return runCatching { Instant.parse(raw).toEpochMilliseconds() }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(raw)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        }.getOrNull()
}

fun beginningOfCurrentMonth(): LocalDateTime {
    val currentInstant = Clock.System.now()
    val currentTZ = TimeZone.currentSystemDefault()
    val currentDate = currentInstant.toLocalDateTime(currentTZ).date

    return LocalDateTime(currentDate.year, currentDate.month, 1, 0, 0)
}

val LocalDateTime.Companion.now: LocalDateTime
    get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun monthRange(year: Int, month: Int): Pair<LocalDate, LocalDate> {
    val fromDate = LocalDate(year, month, 1)
    val toDate = LocalDate(year, month, getLastDayOfMonth(year, month))
    return fromDate to toDate
}

fun computeYearEndDate(year: Int): LocalDate {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return if (year == today.year) today else LocalDate(year, 12, 31)
}

fun getLastDayOfMonth(year: Int, month: Int): Int {
    val firstDayOfThisMonth = LocalDate(year, month, 1)
    val firstDayOfNextMonth = firstDayOfThisMonth.plus(DatePeriod(months = 1))
    val lastDayOfThisMonth = firstDayOfNextMonth.minus(DatePeriod(days = 1))
    return lastDayOfThisMonth.dayOfMonth
}