package com.banko.app.push

import com.banko.app.api.dto.bankoApi.BankAuthDto
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * A local notification Banko should deliver when a bank connection is about to
 * expire or has just expired.
 *
 * iOS cannot receive remote APNs notifications without a paid Apple Developer
 * Program membership, so the platform layer schedules these locally from the
 * backend-provided `expiresAt` value. The backend remains the source of truth;
 * reminders are only refreshed when the app successfully fetches
 * `/Settings/BankAuthorization`.
 */
data class BankReminder(
    val id: String,
    val bankName: String,
    val fireAtEpochMillis: Long,
    /** One of [PushMessageType]'s wire values, reused by the in-app dialog. */
    val wireType: String,
)

/** Prefix shared with the iOS scheduler so stale reminders can be replaced. */
const val BANKO_REMINDER_ID_PREFIX = "banko-eua-"

/**
 * Mirrors the backend's warning window so the reminder and the persistent Home
 * banner appear at roughly the same time.
 */
const val BANK_REMINDER_WARNING_DAYS = 7

private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
private const val DEFAULT_BANK_NAME = "Your bank"

/**
 * Builds the warning and expiry reminders for every authorization that has a
 * parseable, still-future `expiresAt`.
 *
 * Already-expired or already-warning authorizations produce no notification:
 * they are covered by the persistent Home banner, and scheduling a trigger in
 * the past would fire immediately every time the app refreshes.
 */
fun buildBankReminders(
    authorizations: List<BankAuthDto>,
    nowEpochMillis: Long,
    warningDays: Int = BANK_REMINDER_WARNING_DAYS,
): List<BankReminder> {
    if (warningDays < 0) return emptyList()
    val warningOffsetMillis = warningDays * MILLIS_PER_DAY

    return authorizations.flatMap { authorization ->
        val expiresAt = parseExpiresAt(authorization.expiresAt)
            ?: return@flatMap emptyList()
        val bankName = authorization.institutionName
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_BANK_NAME
        val warningAt = expiresAt - warningOffsetMillis

        buildList {
            if (warningAt > nowEpochMillis) {
                add(
                    BankReminder(
                        id = "${BANKO_REMINDER_ID_PREFIX}warning-${authorization.id}",
                        bankName = bankName,
                        fireAtEpochMillis = warningAt,
                        wireType = PushMessageType.EuaExpiryWarning.wireValue,
                    )
                )
            }
            if (expiresAt > nowEpochMillis) {
                add(
                    BankReminder(
                        id = "${BANKO_REMINDER_ID_PREFIX}expired-${authorization.id}",
                        bankName = bankName,
                        fireAtEpochMillis = expiresAt,
                        wireType = PushMessageType.EuaExpiryExpired.wireValue,
                    )
                )
            }
        }
    }
}

/**
 * Accepts both the ISO-8601 instants the API emits (`2026-11-16T12:34:56Z`)
 * and zone-less values, which are interpreted as UTC. Anything else is ignored
 * rather than crashing the Home screen.
 */
private fun parseExpiresAt(value: String?): Long? {
    val raw = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return runCatching { Instant.parse(raw).toEpochMilliseconds() }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(raw)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        }.getOrNull()
}
