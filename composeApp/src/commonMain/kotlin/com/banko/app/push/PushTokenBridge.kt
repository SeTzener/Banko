package com.banko.app.push

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the FCM token received from the platform layer (Android service or iOS
 * messaging delegate) so the shared registration manager can observe it.
 */
class PushTokenBridge {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    fun update(token: String) {
        _token.value = token
    }

    fun current(): String? = _token.value
}
