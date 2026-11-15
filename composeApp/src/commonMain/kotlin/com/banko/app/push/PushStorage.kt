package com.banko.app.push

import com.russhwolf.settings.Settings

/**
 * Persists the last FCM registration token independently of the auth session.
 *
 * The token survives logout so the same installation can be re-registered when
 * a user signs back in. [pendingRegistration] is only a marker: the app always
 * re-registers on launch/auth, which doubles as the retry for a failed send.
 */
class PushStorage(private val settings: Settings = Settings()) {
    var token: String?
        get() = settings.getStringOrNull(KEY_TOKEN)
        set(value) {
            if (value != null) settings.putString(KEY_TOKEN, value)
            else settings.remove(KEY_TOKEN)
        }

    var pendingRegistration: Boolean
        get() = settings.getBoolean(KEY_PENDING_REGISTRATION, false)
        set(value) {
            if (value) settings.putBoolean(KEY_PENDING_REGISTRATION, true)
            else settings.remove(KEY_PENDING_REGISTRATION)
        }

    /**
     * A push message that has not been shown in the app yet. Kept across
     * process death so a notification tap is never silently lost.
     */
    var pendingMessageType: String?
        get() = settings.getStringOrNull(KEY_PENDING_MESSAGE_TYPE)
        set(value) {
            if (value != null) settings.putString(KEY_PENDING_MESSAGE_TYPE, value)
            else settings.remove(KEY_PENDING_MESSAGE_TYPE)
        }

    private companion object {
        const val KEY_TOKEN = "push_token"
        const val KEY_PENDING_REGISTRATION = "push_pending_registration"
        const val KEY_PENDING_MESSAGE_TYPE = "push_pending_message_type"
    }
}
