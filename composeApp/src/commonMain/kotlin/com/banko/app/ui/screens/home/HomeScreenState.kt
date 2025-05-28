package com.banko.app.ui.screens.home

import com.banko.app.ModelTransaction
import com.banko.app.utils.beginningOfCurrentMonth
import kotlinx.datetime.LocalDateTime

data class HomeScreenState(
    private val currentMonth: LocalDateTime = beginningOfCurrentMonth(),
    val transactions: List<ModelTransaction> = emptyList(),
    val totalTransactionCount: Long = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
    val oldestTransactionDate: LocalDateTime = currentMonth,
    val indicatorDateState: LocalDateTime = currentMonth,
    val monthlyTransactions: List<ModelTransaction> = emptyList()
)

sealed class TransactionsEvent {
    data object Refresh : TransactionsEvent()
    data object LoadMore : TransactionsEvent()
    data class ErrorShown(val error: String) : TransactionsEvent()
}