package com.banko.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.pushNew
import com.banko.app.api.auth.AuthState
import com.banko.app.ui.screens.auth.AuthComponent
import com.banko.app.ui.screens.auth.AuthScreen
import com.banko.app.ui.screens.banklinking.BankLinkingScreen
import com.banko.app.ui.screens.details.DetailsScreen
import com.banko.app.ui.screens.details.DetailsScreenViewModel
import com.banko.app.ui.screens.home.HomeScreen
import com.banko.app.ui.screens.profile.ProfileScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.banko.app.ui.screens.navigation.RootComponent
import com.banko.app.ui.screens.navigation.bottomNavItems
import com.banko.app.ui.screens.settings.SettingsScreen
import com.banko.app.ui.theme.BankoTheme
import com.banko.app.ui.theme.Dark_On_Surface
import com.banko.app.ui.theme.Dark_Surface
import com.banko.app.ui.theme.Light_On_Surface
import com.banko.app.ui.theme.Light_Surface

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
                    bottomBar = {
                        BottomNavigation(
                            backgroundColor = if (isSystemInDarkTheme()) Dark_Surface else Light_Surface,
                            contentColor = if (isSystemInDarkTheme()) Dark_On_Surface else Light_On_Surface
                        ) {
                            bottomNavItems.forEach { item ->
                                BottomNavigationItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    selected = childStack.value.active.configuration == item.route,
                                    onClick = {
                                        if (childStack.value.active.configuration != item.route) {
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
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        Children(
                            stack = childStack.value,
                            modifier = Modifier.fillMaxSize(),
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