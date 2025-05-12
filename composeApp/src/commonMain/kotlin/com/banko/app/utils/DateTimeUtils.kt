package com.banko.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


fun beginningOfCurrentMonth(): LocalDateTime {
    val currentInstant = Clock.System.now()
    val currentTZ = TimeZone.currentSystemDefault()
    val currentDate = currentInstant.toLocalDateTime(currentTZ).date

    return LocalDateTime(currentDate.year, currentDate.month, 1, 0, 0)
}