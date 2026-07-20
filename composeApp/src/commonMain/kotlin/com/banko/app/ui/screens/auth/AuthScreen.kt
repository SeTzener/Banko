package com.banko.app.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.banko.app.ui.screens.login.LoginScreen
import com.banko.app.ui.screens.register.RegisterScreen

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun AuthScreen(component: AuthComponent) {
    val childStack = component.childStack.subscribeAsState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Children(
            stack = childStack.value,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation(slide()),
        ) { child ->
            when (val instance = child.instance) {
                is AuthComponent.Child.Login -> LoginScreen(instance.component)
                is AuthComponent.Child.Register -> RegisterScreen(instance.component)
            }
        }
    }
}
