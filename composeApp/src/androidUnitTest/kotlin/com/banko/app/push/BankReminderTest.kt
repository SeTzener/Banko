package com.banko.app.push

import com.banko.app.api.dto.bankoApi.BankAuthDto
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BankReminderTest {

    private val dayMillis = 24L * 60L * 60L * 1000L
    private val now = Instant.parse("2026-01-01T00:00:00Z").toEpochMilliseconds()

    @Test
    fun `builds a warning and an expiry reminder from expiresAt`() {
        val expiresAt = Instant.parse("2026-01-20T12:00:00Z").toEpochMilliseconds()

        val reminders = buildBankReminders(
            authorizations = listOf(authorization("auth-1", "ING", iso(expiresAt))),
            nowEpochMillis = now,
        )

        assertEquals(2, reminders.size)
        val warning = reminders.single { it.wireType == "eua_expiry_warning" }
        assertEquals("banko-eua-warning-auth-1", warning.id)
        assertEquals("ING", warning.bankName)
        assertEquals(expiresAt - 7 * dayMillis, warning.fireAtEpochMillis)

        val expired = reminders.single { it.wireType == "eua_expiry_expired" }
        assertEquals("banko-eua-expired-auth-1", expired.id)
        assertEquals("ING", expired.bankName)
        assertEquals(expiresAt, expired.fireAtEpochMillis)
    }

    @Test
    fun `skips the warning when the bank is already inside the warning window`() {
        val expiresAt = now + 3 * dayMillis

        val reminders = buildBankReminders(
            authorizations = listOf(authorization("auth-1", "ING", iso(expiresAt))),
            nowEpochMillis = now,
        )

        assertEquals(1, reminders.size)
        assertEquals("eua_expiry_expired", reminders.single().wireType)
        assertEquals(expiresAt, reminders.single().fireAtEpochMillis)
    }

    @Test
    fun `skips authorizations that already expired`() {
        val reminders = buildBankReminders(
            authorizations = listOf(
                authorization("auth-1", "ING", iso(now - dayMillis)),
            ),
            nowEpochMillis = now,
        )

        assertTrue(reminders.isEmpty())
    }

    @Test
    fun `ignores missing and unparseable expiry dates`() {
        val reminders = buildBankReminders(
            authorizations = listOf(
                authorization("auth-1", "ING", null),
                authorization("auth-2", "DNB", ""),
                authorization("auth-3", "Nordea", "not-a-date"),
            ),
            nowEpochMillis = now,
        )

        assertTrue(reminders.isEmpty())
    }

    @Test
    fun `parses a zone-less expiry as UTC`() {
        val reminders = buildBankReminders(
            authorizations = listOf(
                authorization("auth-1", "ING", "2099-01-20T12:00:00"),
            ),
            nowEpochMillis = now,
        )

        assertEquals(2, reminders.size)
        assertEquals(
            Instant.parse("2099-01-20T12:00:00Z").toEpochMilliseconds(),
            reminders.single { it.wireType == "eua_expiry_expired" }.fireAtEpochMillis,
        )
    }

    @Test
    fun `falls back to a generic name when the institution is missing`() {
        val reminders = buildBankReminders(
            authorizations = listOf(authorization("auth-1", null, iso(now + 30 * dayMillis))),
            nowEpochMillis = now,
        )

        assertTrue(reminders.isNotEmpty())
        assertTrue(reminders.all { it.bankName == "Your bank" })
    }

    private fun authorization(id: String, name: String?, expiresAt: String?) =
        BankAuthDto(
            id = id,
            institutionName = name,
            expiresAt = expiresAt,
        )

    private fun iso(epochMillis: Long): String =
        Instant.fromEpochMilliseconds(epochMillis).toString()
}
