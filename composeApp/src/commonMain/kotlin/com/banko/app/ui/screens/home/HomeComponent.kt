package com.banko.app.ui.screens.home

import com.arkivanov.decompose.ComponentContext
import com.banko.app.ui.models.Transaction

class HomeComponent(
    componentContext: ComponentContext,
    private val onNavigateToDetails: (Transaction) -> Unit
): ComponentContext by componentContext {
    fun onEvent(event: HomeEvent, transaction: Transaction){
        when(event) {
            HomeEvent.ButtonClick -> onNavigateToDetails(transaction)
        }
    }
}