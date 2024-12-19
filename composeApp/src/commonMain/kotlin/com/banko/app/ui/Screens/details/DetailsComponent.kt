package com.banko.app.ui.Screens.details

import com.arkivanov.decompose.ComponentContext

class DetailsComponent(
    val text: String,
    componentContext: ComponentContext,
    private val onGoBack: () -> Unit
) : ComponentContext by componentContext {
    fun goBack() {
        onGoBack()
    }
}