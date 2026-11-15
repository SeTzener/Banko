package com.banko.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.banko.app.di.initKoin
import com.banko.app.push.PushNotifications
import com.banko.app.push.PushRegistrationManager
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.koin.androidContext
import org.koin.java.KoinJavaComponent

class Banko: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@Banko)
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                isInForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                isInForeground = false
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })

        PushNotifications.createChannel(this)

        val pushRegistrationManager = KoinJavaComponent.getKoin().get<PushRegistrationManager>()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let(pushRegistrationManager::onTokenReceived)
            }
        }
    }

    companion object {
        /**
         * Used to decide whether a foreground message should be explained in the
         * app instead of also posting a system notification.
         */
        var isInForeground: Boolean = false
            private set
    }
}