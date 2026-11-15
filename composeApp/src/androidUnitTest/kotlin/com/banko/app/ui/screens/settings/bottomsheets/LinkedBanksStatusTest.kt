package com.banko.app.ui.screens.settings.bottomsheets

import com.banko.app.api.dto.bankoApi.BankAuthorizationStatus
import com.banko.app.api.dto.bankoApi.BankAuthDto
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkedBanksStatusTest {

    private val now = 1_800_000_000_000L

    private fun authorization(
        status: BankAuthorizationStatus = BankAuthorizationStatus.Processing,
        updatedAt: String = Instant.fromEpochMilliseconds(now).toString(),
    ) = BankAuthDto(
        id = "bank-auth-id",
        institutionId = "BANK",
        status = status,
        updatedAt = updatedAt,
    )

    private fun minutesAgo(minutes: Long): String =
        Instant.fromEpochMilliseconds(now - minutes * 60_000L).toString()

    @Test
    fun `processing authorization counts down to restart`() {
        val auth = authorization(updatedAt = minutesAgo(5))

        assertEquals(10 * 60_000L, auth.processingRemainingMillis(now))
        assertFalse(auth.isProcessingRestartable(now))
    }

    @Test
    fun `processing authorization is restartable after threshold`() {
        val auth = authorization(
            updatedAt = minutesAgo(PROCESSING_STALE_AFTER_MINUTES.toLong()),
        )

        assertEquals(0L, auth.processingRemainingMillis(now))
        assertTrue(auth.isProcessingRestartable(now))
    }

    @Test
    fun `remaining time is clamped to the threshold`() {
        val auth = authorization(
            updatedAt = Instant.fromEpochMilliseconds(now + 60_000L).toString(),
        )

        assertEquals(PROCESSING_STALE_AFTER_MILLIS, auth.processingRemainingMillis(now))
    }

    @Test
    fun `missing timestamp unlocks restart instead of trapping the user`() {
        val auth = authorization(updatedAt = "")

        assertEquals(0L, auth.processingRemainingMillis(now))
        assertTrue(auth.isProcessingRestartable(now))
    }

    @Test
    fun `non processing authorizations have no countdown`() {
        val auth = authorization(
            status = BankAuthorizationStatus.Linked,
            updatedAt = minutesAgo(60),
        )

        assertEquals(0L, auth.processingRemainingMillis(now))
        assertFalse(auth.isProcessingRestartable(now))
    }

    @Test
    fun `countdown rounds up so it never shows zero early`() {
        assertEquals("0:01", formatProcessingCountdown(1L))
        assertEquals("0:01", formatProcessingCountdown(999L))
        assertEquals("0:01", formatProcessingCountdown(1_000L))
        assertEquals("14:32", formatProcessingCountdown(14 * 60_000L + 32_000L))
        assertEquals("15:00", formatProcessingCountdown(PROCESSING_STALE_AFTER_MILLIS))
        assertEquals("0:00", formatProcessingCountdown(0L))
    }
}
