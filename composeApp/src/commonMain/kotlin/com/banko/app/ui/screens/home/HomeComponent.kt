package com.banko.app.ui.screens.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class HomeComponent(
    componentContext: ComponentContext,
    private val onNavigateToDetails: (String) -> Unit
): ComponentContext by componentContext {
    val viewModel = HomeScreenViewModel()
    private var _text = MutableValue("")
    val text: Value<String> = _text

    fun onEvent(event: HomeEvent){
        when(event) {
            HomeEvent.ButtonClick -> onNavigateToDetails(text.value)
            is HomeEvent.UpdateText -> {
                _text.value = event.text
            }
        }
    }
}