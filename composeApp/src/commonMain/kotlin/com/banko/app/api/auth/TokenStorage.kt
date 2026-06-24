package com.banko.app.api.auth

import com.russhwolf.settings.Settings

class TokenStorage(private val settings: Settings = Settings()) {
    var accessToken: String?
        get() = settings.getStringOrNull("access_token")
        set(value) {
            if (value != null) settings.putString("access_token", value)
            else settings.remove("access_token")
        }

    var refreshToken: String?
        get() = settings.getStringOrNull("refresh_token")
        set(value) {
            if (value != null) settings.putString("refresh_token", value)
            else settings.remove("refresh_token")
        }

    var accountId: String?
        get() = settings.getStringOrNull("account_id")
        set(value) {
            if (value != null) settings.putString("account_id", value)
            else settings.remove("account_id")
        }

    fun clear() {
        settings.remove("access_token")
        settings.remove("refresh_token")
        settings.remove("account_id")
    }
}
