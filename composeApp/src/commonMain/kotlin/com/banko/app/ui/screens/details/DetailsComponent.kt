package com.banko.app.ui.screens.details

import com.arkivanov.decompose.ComponentContext
import com.banko.app.ui.models.Transaction

class DetailsComponent(
    val transaction: Transaction,
    componentContext: ComponentContext,
    private val onGoBack: () -> Unit
) : ComponentContext by componentContext {

    fun goBack() {
        onGoBack()
    }
}