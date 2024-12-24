package com.banko.app.ui.screens.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.banko.app.ui.screens.details.DetailsComponent
import com.banko.app.ui.screens.home.HomeComponent
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Configuration>()
    val childStack = childStack(
        source = navigation,
        serializer = Configuration.serializer(),
        initialConfiguration = Configuration.Home,
        handleBackButton = true,
        childFactory = ::createChild
    )

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createChild(
        config: Configuration,
        context: ComponentContext,
    ): Child {
        return when (config) {
            Configuration.Home -> Child.Home(
                HomeComponent(
                    componentContext = context,
                    onNavigateToDetails = { text ->
                        navigation.pushNew(Configuration.Details(text))
                    }
                )
            )

            is Configuration.Details -> Child.Details(
                DetailsComponent(
                    text = config.text,
                    componentContext = context,
                    onGoBack = { navigation.pop() }
                )
            )
        }
    }

    sealed class Child {
        data class Home(val component: HomeComponent) : Child()
        data class Details(val component: DetailsComponent) : Child()
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Home : Configuration()

        @Serializable
        data class Details(val text: String) :
            Configuration() //TODO() Replace text with the Transaction model
    }
}