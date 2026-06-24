package com.banko.app.ui.screens.login

import com.arkivanov.decompose.ComponentContext

class LoginComponent(
    componentContext: ComponentContext,
    val onNavigateToRegister: () -> Unit,
) : ComponentContext by componentContext
