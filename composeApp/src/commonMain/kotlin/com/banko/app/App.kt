package com.banko.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
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
import com.arkivanov.decompose.router.stack.pushNew
import com.banko.app.ui.screens.details.DetailsScreen
import com.banko.app.ui.screens.details.DetailsScreenViewModel
import com.banko.app.ui.screens.home.HomeScreen
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
fun App(root: RootComponent) {
    BankoTheme {
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
                                        root.navigation.pushNew(item.route)
                                    } else {
                                        // This is a workaround to enable navigation to the home screen via the bottom navigation bar
                                        // Without this, navigating to the home screen from other screens results in a crash
                                        root.navigation.popTo(index = 0)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Surface (
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
                    }
                }
            }
        }
    }
}