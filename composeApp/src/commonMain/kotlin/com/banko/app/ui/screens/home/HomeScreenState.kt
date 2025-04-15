package com.banko.app.ui.screens.home

import com.banko.app.ModelTransaction

data class HomeScreenState(
    val transactions: List<ModelTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null
)

sealed class TransactionsEvent {
    data object Refresh : TransactionsEvent()
    data object LoadMore : TransactionsEvent()
    data class ErrorShown(val error: String) : TransactionsEvent()
}