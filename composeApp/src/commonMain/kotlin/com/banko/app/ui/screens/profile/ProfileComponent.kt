package com.banko.app.ui.screens.profile

import com.arkivanov.decompose.ComponentContext

class ProfileComponent(
    componentContext: ComponentContext,
    val onGoBack: () -> Unit,
) : ComponentContext by componentContext {

    fun goBack() {
        onGoBack()
    }
}
