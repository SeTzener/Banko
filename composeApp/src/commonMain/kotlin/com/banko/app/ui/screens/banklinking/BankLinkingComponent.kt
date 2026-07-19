package com.banko.app.ui.screens.banklinking

import com.arkivanov.decompose.ComponentContext

class BankLinkingComponent(
    componentContext: ComponentContext,
    val onGoBack: () -> Unit = {},
    val institutionId: String? = null,
) : ComponentContext by componentContext
