package com.banko.app

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.bringToFront
import com.banko.app.api.auth.AuthState
import com.banko.app.ui.screens.auth.AuthComponent
import com.banko.app.ui.screens.auth.AuthScreen
import com.banko.app.ui.screens.banklinking.BankLinkingScreen
import com.banko.app.ui.screens.details.DetailsScreen
import com.banko.app.ui.screens.home.HomeScreen
import com.banko.app.ui.screens.profile.ProfileScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.banko.app.ui.screens.navigation.RootComponent
import com.banko.app.ui.screens.navigation.bottomNavItems
import com.banko.app.ui.screens.settings.SettingsScreen
import com.banko.app.ui.theme.BankoTheme

@OptIn(ExperimentalDecomposeApi::class)
@Composable
@Preview
fun App(root: RootComponent, authComponent: AuthComponent) {
    val authState by root.sessionManager.authState.collectAsState()

    BankoTheme {
        when (authState) {
            AuthState.Loading -> {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }
            AuthState.Unauthenticated -> {
                AuthScreen(component = authComponent)
            }
            AuthState.Authenticated -> {
                val childStack = root.childStack.subscribeAsState()
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surface,
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = childStack.value.active.configuration == item.route
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    selected = selected,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        indicatorColor = MaterialTheme.colorScheme.surface
                                    ),
                                    onClick = {
                                        if (!selected) {
                                            if (item.route != RootComponent.Configuration.Home) {
                                                root.navigation.bringToFront(item.route)
                                            } else {
                                                root.navigation.popTo(index = 0)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier.padding(paddingValues),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Children(
                            stack = childStack.value,
                            modifier = Modifier.fillMaxSize().swipeToGoBack(
                                enabled = childStack.value.items.size > 1,
                                onBack = { root.navigation.pop() },
                            ),
                            animation = stackAnimation(slide()),
                        ) { child ->
                            when (val instance = child.instance) {
                                is RootComponent.Child.Home -> HomeScreen(instance.component)
                                is RootComponent.Child.Details -> DetailsScreen(
                                    component = instance.component,
                                )
                                is RootComponent.Child.Settings -> SettingsScreen(instance.component)
                                is RootComponent.Child.Profile -> ProfileScreen(instance.component)
                                is RootComponent.Child.BankLinking -> BankLinkingScreen(instance.component)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.swipeToGoBack(
    enabled: Boolean = true,
    onBack: () -> Unit,
): Modifier = if (!enabled) this else pointerInput(Unit) {
    val edgeThresholdPx = with(density) { 24.dp.toPx() }
    val distanceThresholdPx = with(density) { 100.dp.toPx() }
    var totalDragX = 0f
    var dragFromEdge = false

    detectHorizontalDragGestures(
        onDragStart = { offset ->
            totalDragX = 0f
            dragFromEdge = offset.x < edgeThresholdPx
        },
        onHorizontalDrag = { _, dragAmount ->
            if (dragFromEdge && dragAmount > 0) {
                totalDragX += dragAmount
                if (totalDragX > distanceThresholdPx) {
                    onBack()
                    dragFromEdge = false
                }
            }
        },
        onDragEnd = { dragFromEdge = false },
        onDragCancel = { dragFromEdge = false },
    )
}