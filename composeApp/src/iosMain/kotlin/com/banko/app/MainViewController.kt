package com.banko.app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.banko.app.ui.screens.navigation.RootComponent

fun MainViewController() = ComposeUIViewController {
    val root = remember { RootComponent(DefaultComponentContext(LifecycleRegistry())) }
    val preferences = remember { createDataStore() }
    App(
        root = root,
        prefs = preferences
    )
}
