package com.banko.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.banko.app.api.auth.SessionManager
import com.banko.app.di.initKoin
import com.banko.app.ui.screens.auth.AuthComponent
import com.banko.app.ui.screens.navigation.RootComponent
import org.koin.compose.koinInject

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    val sessionManager = koinInject<SessionManager>()
    val root = remember {
        RootComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            sessionManager = sessionManager,
        )
    }
    val authComponent = remember {
        AuthComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
        )
    }
    App(
        root = root,
        authComponent = authComponent,
    )
}
