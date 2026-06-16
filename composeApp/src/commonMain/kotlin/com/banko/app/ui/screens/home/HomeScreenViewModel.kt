package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.domain.repository.TransactionRepository
import com.banko.app.ui.models.toUi
import com.banko.app.utils.beginningOfCurrentMonth
import com.banko.app.utils.monthRange
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class HomeScreenViewModel(
    private val repository: TransactionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private var transactionsJob: Job? = null
    private var syncJob: Job? = null

    init {
        initializeTimespanSelection()
    }

    override fun onCleared() {
        super.onCleared()
        transactionsJob?.cancel()
        syncJob?.cancel()
    }

    fun handleEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.Refresh -> refreshData()
            is TransactionsEvent.ErrorShown -> clearError(event.error)
            is TransactionsEvent.DeleteTransaction -> handleDeleteTransaction(event.transactionId)
            is TransactionsEvent.SelectTimespan -> handleSelectTimespan(event.timespan)
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            try {
                val state = _state.value
                val sel = state.selectedTimespan as TimespanSelection.Month
                val (fromDate, toDate) = monthRange(sel.ym.year, sel.ym.month)
                repository.fetchAndStoreTransactionsForDateRange(fromDate, toDate)
                loadTransactionsForCurrentSelection()
            } catch (_: Exception) {
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun clearError(error: String) {
        if (_state.value.error == error) {
            _state.update { it.copy(error = null) }
        }
    }

    private fun handleDeleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transactionId)
                _state.update { state ->
                    state.copy(
                        transactions = state.transactions.filter { it.id != transactionId }
                    )
                }
            } catch (ex: Exception) {
                _state.update {
                    it.copy(error = ex.message)
                }
            }
        }
    }

    private fun initializeTimespanSelection() {
        viewModelScope.launch {
            delay(200)

            val oldestDate = repository.getOldestTransactions()
            val months = generateMonthRange(oldestDate)
            _state.update { it.copy(availableMonths = months) }

            // Bootstrap if the DB has very few months of data
            if (months.size < 24) {
                try {
                    val now = beginningOfCurrentMonth()
                    val fromDate = LocalDate(now.year - 3, 1, 1)
                    val toDate = LocalDate(now.year, now.monthNumber, now.dayOfMonth)
                    repository.fetchAndStoreTransactionsForDateRange(fromDate, toDate)

                    val refreshedOldestDate = repository.getOldestTransactions()
                    val refreshedMonths = generateMonthRange(refreshedOldestDate)
                    _state.update { it.copy(availableMonths = refreshedMonths) }
                } catch (_: Exception) { }
            }

            loadTransactionsForCurrentSelection()
            scheduleBackgroundSync()
        }
    }

    private fun loadTransactionsForCurrentSelection() {
        val state = _state.value
        transactionsJob?.cancel()

        val sel = state.selectedTimespan as TimespanSelection.Month
        val (fromDate, toDate) = monthRange(sel.ym.year, sel.ym.month)

        transactionsJob = repository.getTransactionsForDateRange(fromDate, toDate)
            .map { list -> list.map { it.toUi() } }
            .onEach { transactions ->
                _state.update {
                    it.copy(transactions = transactions, isLoading = false, isRefreshing = false)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun scheduleBackgroundSync() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            delay(500)
            performBackgroundSync()
        }
    }

    private suspend fun performBackgroundSync() {
        val state = _state.value
        val sel = state.selectedTimespan as TimespanSelection.Month
        val (fromDate, toDate) = monthRange(sel.ym.year, sel.ym.month)
        try {
            repository.fetchAndStoreTransactionsForDateRange(fromDate, toDate)
            val oldestDate = repository.getOldestTransactions()
            val months = generateMonthRange(oldestDate)
            if (months.size > _state.value.availableMonths.size) {
                _state.update { it.copy(availableMonths = months) }
            }
        } catch (_: Exception) {
            // silent background sync failure
        }
    }

    private fun handleSelectTimespan(timespan: TimespanSelection) {
        val monthTimespan = timespan as TimespanSelection.Month
        _state.update {
            it.copy(
                selectedTimespan = timespan,
                indicatorDateState = LocalDateTime(
                    monthTimespan.ym.year, monthTimespan.ym.month, 1, 0, 0
                )
            )
        }
        loadTransactionsForCurrentSelection()
        scheduleBackgroundSync()
    }

    private fun generateMonthRange(from: LocalDateTime): List<YearMonth> {
        val now = beginningOfCurrentMonth()
        val months = mutableListOf<YearMonth>()
        var year = now.year
        var month = now.monthNumber
        while (year > from.year || (year == from.year && month >= from.monthNumber)) {
            months.add(YearMonth(year, month))
            month--
            if (month == 0) {
                year--
                month = 12
            }
        }
        if (months.isEmpty()) {
            months.add(YearMonth(now.year, now.monthNumber))
        }
        return months
    }
}
