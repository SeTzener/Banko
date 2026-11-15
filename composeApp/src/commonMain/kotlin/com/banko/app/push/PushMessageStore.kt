package com.banko.app.push

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the push message that should be explained inside the app.
 *
 * It is persisted so a notification tapped while the app is starting (or while
 * the user is still signed out) is not lost before the UI can show it.
 */
class PushMessageStore(private val pushStorage: PushStorage) {
    private val _message = MutableStateFlow(pushStorage.pendingMessageType.toPushMessage())
    val message: StateFlow<PushMessage?> = _message.asStateFlow()

    fun publishType(type: String?) {
        val message = type.toPushMessage() ?: return
        pushStorage.pendingMessageType = message.type.wireValue
        _message.value = message
    }

    fun dismiss() {
        pushStorage.pendingMessageType = null
        _message.value = null
    }

    private fun String?.toPushMessage(): PushMessage? =
        PushMessageType.fromWire(this)?.let(::PushMessage)
}
