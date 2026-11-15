package com.banko.app.push

/**
 * Platform hook for replacing all pending Banko expiry reminders.
 *
 * Android leaves the delegate unset, so reminders are a no-op there and FCM
 * remains the only notification source. iOS implements this with
 * `UNUserNotificationCenter` and registers itself in `AppDelegate`.
 */
interface BankReminderScheduler {
    fun replaceAll(reminders: List<BankReminder>)
}

/**
 * Connects the shared code to the iOS implementation. Swift owns the delegate
 * because Kotlin cannot call into Swift directly.
 */
object BankReminderBridge : BankReminderScheduler {
    var delegate: BankReminderScheduler? = null

    override fun replaceAll(reminders: List<BankReminder>) {
        delegate?.replaceAll(reminders)
    }
}
