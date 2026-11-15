package com.banko.app.push

import org.koin.mp.KoinPlatform

/**
 * Entry point used by the iOS app delegate. Firebase can deliver a token before
 * the Compose/Koin graph has been created, so the token is persisted and the
 * registration manager picks it up when it is constructed.
 */
object PushTokenReceiver {
    fun handle(token: String) {
        if (token.isBlank()) return

        val manager = KoinPlatform.getKoinOrNull()?.get<PushRegistrationManager>()
        if (manager != null) {
            manager.onTokenReceived(token)
        } else {
            PushStorage().token = token
        }
    }
}
