package com.banko.app.ui.screens.home

import com.banko.app.ModelTransaction
import com.banko.app.utils.beginningOfCurrentMonth
import com.banko.app.utils.computeYearEndDate
import com.banko.app.utils.getLastDayOfMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class YearMonth(val year: Int, val month: Int)

sealed class TimespanSelection {
    abstract val fromDate: LocalDate
    abstract val toDate: LocalDate

    data class Month(val ym: YearMonth) : TimespanSelection() {
        override val fromDate: LocalDate = LocalDate(ym.year, ym.month, 1)
        override val toDate: LocalDate = LocalDate(ym.year, ym.month, getLastDayOfMonth(ym.year, ym.month))
    }

    data class Year(val year: Int) : TimespanSelection() {
        override val fromDate: LocalDate = LocalDate(year, 1, 1)
        override val toDate: LocalDate = computeYearEndDate(year)
    }
}

data class TransactionListState(
    val transactions: List<ModelTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
)

data class TimespanState(
    val selectedTimespan: TimespanSelection = TimespanSelection.Month(YearMonth(beginningOfCurrentMonth().year, beginningOfCurrentMonth().monthNumber)),
    val availableMonths: List<YearMonth> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val isYearView: Boolean = false,
    val indicatorDateState: LocalDateTime = beginningOfCurrentMonth(),
)

data class UiState(
    val error: String? = null,
    val isSyncing: Boolean = false,
)

data class HomeScreenState(
    val transactions: List<ModelTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val indicatorDateState: LocalDateTime = beginningOfCurrentMonth(),
    val selectedTimespan: TimespanSelection = TimespanSelection.Month(YearMonth(beginningOfCurrentMonth().year, beginningOfCurrentMonth().monthNumber)),
    val availableMonths: List<YearMonth> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val isYearView: Boolean = false,
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val selectedTagId: String? = null,
)

sealed class TransactionsEvent {
    data object Refresh : TransactionsEvent()
    data class ErrorShown(val error: String) : TransactionsEvent()
    data class DeleteTransaction(val transactionId: String) : TransactionsEvent()
    data class SelectTimespan(val timespan: TimespanSelection) : TransactionsEvent()
    data object ToggleTimespanView : TransactionsEvent()
    data object LoadMore : TransactionsEvent()
    data class SelectTag(val tagId: String?) : TransactionsEvent()
}
