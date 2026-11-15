package com.banko.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.banko.app.api.auth.SessionManager
import com.banko.app.push.PushMessageStore
import com.banko.app.push.PushRegistrationManager
import com.banko.app.ui.screens.auth.AuthComponent
import com.banko.app.ui.screens.navigation.RootComponent
import org.koin.java.KoinJavaComponent

class MainActivity : ComponentActivity() {
    private val pushMessageStore: PushMessageStore
        get() = KoinJavaComponent.get(PushMessageStore::class.java)

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        val sessionManager: SessionManager by lazy { KoinJavaComponent.get(SessionManager::class.java) }
        val root = retainedComponent {
            RootComponent(
                componentContext = it,
                sessionManager = sessionManager,
                pushMessageStore = pushMessageStore,
            )
        }
        val authComponent = AuthComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry())
        )
        handlePushIntent(intent)
        setContent {
            App(root = root, authComponent = authComponent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
    }

    /**
     * Turns a tapped push into an in-app explanation. The payload is consumed
     * so a configuration change does not republish a message already dismissed.
     */
    private fun handlePushIntent(intent: Intent?) {
        val type = intent?.getStringExtra(PUSH_TYPE_EXTRA) ?: return
        pushMessageStore.publishType(type)
        intent.removeExtra(PUSH_TYPE_EXTRA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            KoinJavaComponent.getKoin()
                .get<PushRegistrationManager>()
                .registerInBackground()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST,
            )
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST = 1001
        const val PUSH_TYPE_EXTRA = "type"
    }
}