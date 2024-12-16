package com.banko.app.ui.Screens.home

sealed interface HomeEvent {
    data object buttonClick: HomeEvent
    data class UpdateText(val text: String): HomeEvent
}