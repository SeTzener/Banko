package com.banko.app.ui.screens.home

import com.banko.app.ModelTransaction

data class HomeScreenState(
    val transactions: List<ModelTransaction> = emptyList(),
    val isLoading: Boolean = false
)