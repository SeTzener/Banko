package com.banko.app.push

import org.koin.mp.KoinPlatform

/**
 * Entry point used by the iOS notification delegate. Returns true when the
 * payload is a message Banko knows how to explain in the app.
 */
object PushMessageReceiver {
    fun handle(type: String?): Boolean {
        val parsed = PushMessageType.fromWire(type) ?: return false

        val store = KoinPlatform.getKoinOrNull()?.get<PushMessageStore>()
        if (store != null) {
            store.publishType(parsed.wireValue)
        } else {
            // Koin may not be ready yet during a cold start; the store restores
            // the persisted type when it is created.
            PushStorage().pendingMessageType = parsed.wireValue
        }
        return true
    }
}
