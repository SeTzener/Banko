package com.banko.app.ui.screens.register

import com.arkivanov.decompose.ComponentContext

class RegisterComponent(
    componentContext: ComponentContext,
    val onNavigateToLogin: () -> Unit,
) : ComponentContext by componentContext
