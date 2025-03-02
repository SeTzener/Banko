package com.banko.app.ui.screens.home

import com.banko.app.ModelTransaction

data class HomeScreenState(
    val transactions: List<ModelTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val dbTransactionsCount: Long = 0,
    val apiTransactionsCount: Long = 0,
)