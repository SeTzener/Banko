package com.banko.app.ui.screens.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.banko.app.ui.screens.login.LoginComponent
import com.banko.app.ui.screens.register.RegisterComponent
import kotlinx.serialization.Serializable

class AuthComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    val navigation = StackNavigation<Configuration>()
    val childStack = childStack(
        source = navigation,
        serializer = Configuration.serializer(),
        initialConfiguration = Configuration.Login,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createChild(
        config: Configuration,
        context: ComponentContext,
    ): Child {
        return when (config) {
            is Configuration.Login -> Child.Login(
                LoginComponent(
                    componentContext = context,
                    onNavigateToRegister = { navigation.pushNew(Configuration.Register) },
                )
            )
            is Configuration.Register -> Child.Register(
                RegisterComponent(
                    componentContext = context,
                    onNavigateToLogin = { navigation.pop() },
                )
            )
        }
    }

    sealed class Child {
        data class Login(val component: LoginComponent) : Child()
        data class Register(val component: RegisterComponent) : Child()
    }

    @Serializable
    sealed class Configuration {
        @Serializable data object Login : Configuration()
        @Serializable data object Register : Configuration()
    }
}
