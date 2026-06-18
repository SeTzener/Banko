package com.banko.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTimeUtilsTest {

    @Test
    fun `beginningOfCurrentMonth should return first day of current month`() {
        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val result = beginningOfCurrentMonth()

        assertEquals(now.year, result.year)
        assertEquals(now.monthNumber, result.monthNumber)
        assertEquals(1, result.dayOfMonth)
        assertEquals(0, result.hour)
        assertEquals(0, result.minute)
    }

    @Test
    fun `monthRange should return correct range for January`() {
        val (from, to) = monthRange(2024, 1)
        assertEquals(LocalDate(2024, 1, 1), from)
        assertEquals(LocalDate(2024, 1, 31), to)
    }

    @Test
    fun `monthRange should return correct range for February in leap year`() {
        val (from, to) = monthRange(2024, 2)
        assertEquals(LocalDate(2024, 2, 1), from)
        assertEquals(LocalDate(2024, 2, 29), to)
    }

    @Test
    fun `monthRange should return correct range for February in non-leap year`() {
        val (from, to) = monthRange(2023, 2)
        assertEquals(LocalDate(2023, 2, 1), from)
        assertEquals(LocalDate(2023, 2, 28), to)
    }

    @Test
    fun `computeYearEndDate for past year should return Dec 31`() {
        val result = computeYearEndDate(2020)
        assertEquals(LocalDate(2020, 12, 31), result)
    }

    @Test
    fun `getLastDayOfMonth for January`() {
        assertEquals(31, getLastDayOfMonth(2024, 1))
    }

    @Test
    fun `getLastDayOfMonth for February leap year`() {
        assertEquals(29, getLastDayOfMonth(2024, 2))
    }

    @Test
    fun `getLastDayOfMonth for February non-leap year`() {
        assertEquals(28, getLastDayOfMonth(2023, 2))
    }

    @Test
    fun `getLastDayOfMonth for April`() {
        assertEquals(30, getLastDayOfMonth(2024, 4))
    }

    @Test
    fun `getLastDayOfMonth for December`() {
        assertEquals(31, getLastDayOfMonth(2024, 12))
    }
}
