package com.banko.app.ui.screens.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: RootComponent.Configuration, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(RootComponent.Configuration.Home, "Home", Icons.Default.Home)
    object Settings : BottomNavItem(RootComponent.Configuration.Settings, "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Settings
)
