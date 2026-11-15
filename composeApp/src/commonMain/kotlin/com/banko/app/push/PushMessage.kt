package com.banko.app.push

/**
 * The notification types sent by the backend. The wire value is the `type`
 * entry of the FCM data payload.
 */
enum class PushMessageType(val wireValue: String) {
    EuaExpiryWarning("eua_expiry_warning"),
    EuaExpiryExpired("eua_expiry_expired");

    companion object {
        fun fromWire(value: String?): PushMessageType? =
            entries.firstOrNull { it.wireValue == value }
    }
}

data class PushMessage(val type: PushMessageType)
