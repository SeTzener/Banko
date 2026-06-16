package com.banko.app.ui.screens.home

import com.banko.app.ModelTransaction
import com.banko.app.utils.beginningOfCurrentMonth
import kotlinx.datetime.LocalDateTime

data class YearMonth(val year: Int, val month: Int)

sealed class TimespanSelection {
    data class Month(val ym: YearMonth) : TimespanSelection()
}

data class HomeScreenState(
    val transactions: List<ModelTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val indicatorDateState: LocalDateTime = beginningOfCurrentMonth(),
    val selectedTimespan: TimespanSelection = TimespanSelection.Month(YearMonth(beginningOfCurrentMonth().year, beginningOfCurrentMonth().monthNumber)),
    val availableMonths: List<YearMonth> = emptyList(),
    val isSyncing: Boolean = false,
)

sealed class TransactionsEvent {
    data object Refresh : TransactionsEvent()
    data class ErrorShown(val error: String) : TransactionsEvent()
    data class DeleteTransaction(val transactionId: String) : TransactionsEvent()
    data class SelectTimespan(val timespan: TimespanSelection) : TransactionsEvent()
}
