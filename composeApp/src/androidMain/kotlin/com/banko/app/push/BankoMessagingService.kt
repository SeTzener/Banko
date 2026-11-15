package com.banko.app.push

import com.banko.app.Banko
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.java.KoinJavaComponent

class BankoMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        KoinJavaComponent.getKoin()
            .get<PushRegistrationManager>()
            .onTokenReceived(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"]

        // While the app is visible, explain the push with a dialog instead of
        // posting a second notification the user would have to tap.
        if (Banko.isInForeground && PushMessageType.fromWire(type) != null) {
            KoinJavaComponent.getKoin()
                .get<PushMessageStore>()
                .publishType(type)
            return
        }

        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"]
        if (title.isNullOrBlank() && body.isNullOrBlank()) return

        PushNotifications.show(this, title, body, message.data)
    }
}
