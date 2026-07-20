package com.banko.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.banko.app.api.auth.SessionManager
import com.banko.app.ui.screens.auth.AuthComponent
import com.banko.app.ui.screens.navigation.RootComponent
import org.koin.java.KoinJavaComponent

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sessionManager: SessionManager by lazy { KoinJavaComponent.get(SessionManager::class.java) }
        val root = retainedComponent {
            RootComponent(
                componentContext = it,
                sessionManager = sessionManager,
            )
        }
        setContent {
            val authComponent = remember {
                AuthComponent(
                    componentContext = DefaultComponentContext(LifecycleRegistry())
                )
            }
            App(root = root, authComponent = authComponent)
        }
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}