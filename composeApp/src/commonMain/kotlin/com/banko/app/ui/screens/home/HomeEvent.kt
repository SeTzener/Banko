package com.banko.app.ui.screens.home

sealed interface HomeEvent {
    data object ButtonClick: HomeEvent
    data class UpdateText(val text: String): HomeEvent
}