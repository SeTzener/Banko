package com.banko.app.ui.screens.settings.bottomsheets

import com.banko.app.api.dto.bankoApi.BankAuthorizationStatus
import com.banko.app.api.dto.bankoApi.BankAuthDto
import com.banko.app.utils.parseApiInstant

/**
 * How long an authorization may stay in [BankAuthorizationStatus.Processing]
 * before the app offers to restart it. The backend has no background job that
 * reconciles abandoned links, so this only gates the restart button.
 */
internal const val PROCESSING_STALE_AFTER_MINUTES = 15
internal const val PROCESSING_STALE_AFTER_MILLIS = PROCESSING_STALE_AFTER_MINUTES * 60_000L

private const val MILLIS_PER_SECOND = 1_000L

/**
 * Milliseconds left before a Processing authorization can be restarted.
 *
 * The value is derived from the backend's `updatedAt` on every tick, so
 * backgrounding the app or restarting the process cannot make the countdown
 * drift. Missing or malformed timestamps return 0 so the user is never
 * trapped behind a button that can never unlock.
 */
internal fun BankAuthDto.processingRemainingMillis(nowEpochMillis: Long): Long {
    if (status != BankAuthorizationStatus.Processing) return 0L

    val startedAt = parseApiInstant(updatedAt) ?: return 0L
    return (startedAt + PROCESSING_STALE_AFTER_MILLIS - nowEpochMillis)
        .coerceIn(0L, PROCESSING_STALE_AFTER_MILLIS)
}

internal fun BankAuthDto.isProcessingRestartable(nowEpochMillis: Long): Boolean =
    status == BankAuthorizationStatus.Processing &&
        processingRemainingMillis(nowEpochMillis) == 0L

/** Formats the remaining time as `m:ss`, rounding up so it never shows 0:00 early. */
internal fun formatProcessingCountdown(remainingMillis: Long): String {
    val totalSeconds = ((remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND)
        .coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
