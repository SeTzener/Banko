package com.banko.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.banko.app.ui.screens.details.DetailScreenViewModel
import com.banko.app.ui.screens.details.DetailsScreen
import com.banko.app.ui.screens.home.HomeScreen
import com.banko.app.ui.screens.navigation.RootComponent
import com.banko.app.ui.theme.BankoTheme

@Composable
@Preview
fun App(root: RootComponent) {
    BankoTheme {
        Surface {
            val childStack by root.childStack.subscribeAsState()
            Children(
                stack = childStack,
                modifier = Modifier.fillMaxSize(),
                animation = stackAnimation(slide()),
            ) { child ->
                when (val instance = child.instance) {
                    is RootComponent.Child.Home -> HomeScreen(instance.component)
                    // TODO() add koin to the project and change -> DetailScreenViewModel()
                    is RootComponent.Child.Details -> DetailsScreen(component = instance.component, viewModel = DetailScreenViewModel())
                }
            }
        }
    }
}