package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.data.repository.TransactionRepository
import com.banko.app.ui.models.toUi
import com.banko.app.utils.beginningOfCurrentMonth
import com.banko.app.utils.computeYearEndDate
import com.banko.app.utils.getLastDayOfMonth
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

private const val SYNC_COOLDOWN_MS = 300_000L

class HomeScreenViewModel(
    private val repository: TransactionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    val transactionListState: StateFlow<TransactionListState> = _state.map { s ->
        TransactionListState(
            transactions = s.transactions,
            isLoading = s.isLoading,
            isRefreshing = s.isRefreshing,
            isLoadingMore = s.isLoadingMore,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TransactionListState())

    val filteredTransactionListState: StateFlow<TransactionListState> = _state.map { s ->
        TransactionListState(
            transactions = when (s.selectedTagId) {
                null -> s.transactions
                "uncategorized" -> s.transactions.filter { it.expenseTag == null }
                else -> s.transactions.filter { it.expenseTag?.id == s.selectedTagId }
            },
            isLoading = s.isLoading,
            isRefreshing = s.isRefreshing,
            isLoadingMore = s.isLoadingMore,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TransactionListState())

    val timespanState: StateFlow<TimespanState> = _state.map { s ->
        TimespanState(
            selectedTimespan = s.selectedTimespan,
            availableMonths = s.availableMonths,
            availableYears = s.availableYears,
            isYearView = s.isYearView,
            indicatorDateState = s.indicatorDateState,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TimespanState())

    val uiState: StateFlow<UiState> = _state.map { s ->
        UiState(
            error = s.error,
            isSyncing = s.isSyncing,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    val selectedTagId: StateFlow<String?> = _state.map { it.selectedTagId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var transactionsJob: Job? = null
    private var syncJob: Job? = null
    private var lastMonthSelection: YearMonth? = null
    private val lastSyncTimestamps = mutableMapOf<String, Long>()

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
            is TransactionsEvent.ToggleTimespanView -> handleToggleView()
            is TransactionsEvent.LoadMore -> handleLoadMore()
            is TransactionsEvent.SelectTag -> handleSelectTag(event.tagId)
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            try {
                val sel = _state.value.selectedTimespan
                repository.fetchAndStoreTransactionsForDateRange(sel.fromDate, sel.toDate)
                lastSyncTimestamps["${sel.fromDate}-${sel.toDate}"] = Clock.System.now().toEpochMilliseconds()
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
                _state.update { it.copy(error = ex.message) }
            }
        }
    }

    private fun initializeTimespanSelection() {
        viewModelScope.launch {
            delay(200)

            // Show cached DB data immediately
            val oldestDate = repository.getOldestTransactions()
            val months = generateMonthRange(oldestDate)
            val years = months.map { it.year }.distinct().sortedDescending()
            _state.update { it.copy(availableMonths = months, availableYears = years) }
            loadTransactionsForCurrentSelection()

            // Sync in background — never block screen init
            if (months.size < 24) {
                viewModelScope.launch {
                    delay(500)
                    bootstrapData()
                }
            } else {
                scheduleBackgroundSync()
            }
        }
    }

    private suspend fun bootstrapData() {
        try {
            val now = beginningOfCurrentMonth()
            val fromDate = LocalDate(now.year - 3, 1, 1)
            val toDate = LocalDate(now.year, now.monthNumber, now.dayOfMonth)
            repository.fetchAndStoreTransactionsForDateRange(fromDate, toDate)

            val refreshedOldestDate = repository.getOldestTransactions()
            val refreshedMonths = generateMonthRange(refreshedOldestDate)
            val refreshedYears = refreshedMonths.map { it.year }.distinct().sortedDescending()
            _state.update { it.copy(availableMonths = refreshedMonths, availableYears = refreshedYears) }
        } catch (_: Exception) { }
    }

    private fun loadTransactionsForCurrentSelection() {
        val state = _state.value
        transactionsJob?.cancel()

        val sel = state.selectedTimespan
        transactionsJob = repository.getTransactionsForDateRange(sel.fromDate, sel.toDate)
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
        val sel = _state.value.selectedTimespan
        val rangeKey = "${sel.fromDate}-${sel.toDate}"
        val now = Clock.System.now().toEpochMilliseconds()
        val lastSync = lastSyncTimestamps[rangeKey]

        if (lastSync != null && now - lastSync < SYNC_COOLDOWN_MS) return

        try {
            repository.fetchAndStoreTransactionsForDateRange(sel.fromDate, sel.toDate)
            lastSyncTimestamps[rangeKey] = Clock.System.now().toEpochMilliseconds()
            val oldestDate = repository.getOldestTransactions()
            val months = generateMonthRange(oldestDate)
            if (months.size > _state.value.availableMonths.size) {
                val years = months.map { it.year }.distinct().sortedDescending()
                _state.update { it.copy(availableMonths = months, availableYears = years) }
            }
        } catch (_: Exception) {
            // silent background sync failure
        }
    }

    private fun handleSelectTag(tagId: String?) {
        val normalizedTagId = tagId ?: "uncategorized"
        _state.update {
            if (it.selectedTagId == normalizedTagId) {
                it.copy(selectedTagId = null)
            } else {
                it.copy(selectedTagId = normalizedTagId)
            }
        }
    }

    private fun handleSelectTimespan(timespan: TimespanSelection) {
        when (timespan) {
            is TimespanSelection.Month -> {
                lastMonthSelection = timespan.ym
                _state.update {
                    it.copy(
                        selectedTimespan = timespan,
                        indicatorDateState = LocalDateTime(timespan.ym.year, timespan.ym.month, 1, 0, 0),
                        isYearView = false,
                        selectedTagId = null,
                    )
                }
            }
            is TimespanSelection.Year -> {
                _state.update {
                    it.copy(
                        selectedTimespan = timespan,
                        indicatorDateState = LocalDateTime(timespan.year, 1, 1, 0, 0),
                        isYearView = true,
                        selectedTagId = null,
                    )
                }
            }
        }
        loadTransactionsForCurrentSelection()
        scheduleBackgroundSync()
    }

    private fun handleToggleView() {
        val state = _state.value
        if (state.isYearView) {
            val month = lastMonthSelection ?: YearMonth(
                beginningOfCurrentMonth().year,
                beginningOfCurrentMonth().monthNumber
            )
            handleSelectTimespan(TimespanSelection.Month(month))
        } else {
            val ym = (state.selectedTimespan as? TimespanSelection.Month)?.ym
                ?: YearMonth(beginningOfCurrentMonth().year, beginningOfCurrentMonth().monthNumber)
            lastMonthSelection = ym
            handleSelectTimespan(TimespanSelection.Year(ym.year))
        }
    }

    private fun handleLoadMore() {
        val currentState = _state.value
        if (currentState.isLoadingMore) return

        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                val s = _state.value
                val (fromDate, toDate) = if (s.isYearView) {
                    val oldestYear = s.availableYears.last()
                    LocalDate(oldestYear - 1, 1, 1) to computeYearEndDate(oldestYear - 1)
                } else {
                    val oldestMonth = s.availableMonths.last()
                    val prevYear = if (oldestMonth.month == 1) oldestMonth.year - 1 else oldestMonth.year
                    val prevMonth = if (oldestMonth.month == 1) 12 else oldestMonth.month - 1
                    LocalDate(prevYear, prevMonth, 1) to LocalDate(prevYear, prevMonth, getLastDayOfMonth(prevYear, prevMonth))
                }

                repository.fetchAndStoreTransactionsForDateRange(fromDate, toDate)

                val refreshedOldestDate = repository.getOldestTransactions()
                val refreshedMonths = generateMonthRange(refreshedOldestDate)
                val refreshedYears = refreshedMonths.map { it.year }.distinct().sortedDescending()
                _state.update {
                    it.copy(
                        availableMonths = refreshedMonths,
                        availableYears = refreshedYears,
                        isLoadingMore = false
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
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
