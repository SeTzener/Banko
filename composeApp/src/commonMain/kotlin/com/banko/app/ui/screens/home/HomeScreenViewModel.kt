package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.data.repository.CurrencyRepository
import com.banko.app.data.repository.TransactionRepository
import com.banko.app.domain.CurrencyPreferences
import com.banko.app.domain.model.isCurrencySupported
import kotlin.math.roundToLong
import com.banko.app.ui.models.toUi
import com.banko.app.ui.utils.ErrorState
import com.banko.app.ui.utils.classifyError
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
import kotlinx.coroutines.flow.combine
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
    private val repository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val currencyPreferences: CurrencyPreferences,
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
            transactions = when (val filter = s.tagFilter) {
                is TagFilter.None -> s.transactions
                is TagFilter.Uncategorized -> s.transactions.filter { it.expenseTag == null }
                is TagFilter.ById -> s.transactions.filter { it.expenseTag?.id == filter.id }
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

    val selectedCurrency: StateFlow<String> = currencyPreferences.selectedCurrency
        .stateIn(viewModelScope, SharingStarted.Eagerly, CurrencyPreferences.DEFAULT_CURRENCY)

    val selectedCategoryId: StateFlow<String?> = _state.map { s ->
        when (s.tagFilter) {
            is TagFilter.None -> null
            is TagFilter.Uncategorized -> null
            is TagFilter.ById -> s.tagFilter.id
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isUncategorizedSelected: StateFlow<Boolean> = _state.map { s ->
        s.tagFilter is TagFilter.Uncategorized
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
            } catch (e: Exception) {
                _state.update { it.copy(isRefreshing = false, error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
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
                _state.update { it.copy(error = ErrorState(classifyError(ex), ex.message)) }
            }
        }
    }

    private fun initializeTimespanSelection() {
        viewModelScope.launch {
            delay(200)

            val oldestDate = repository.getOldestTransactions()
            val months = generateMonthRange(oldestDate)
            val years = months.map { it.year }.distinct().sortedDescending()
            _state.update { it.copy(availableMonths = months, availableYears = years) }
            loadTransactionsForCurrentSelection()

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
        } catch (e: Exception) {
            _state.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
        }
    }

    private fun loadTransactionsForCurrentSelection() {
        val state = _state.value
        transactionsJob?.cancel()

        val sel = state.selectedTimespan
        transactionsJob = combine(
            repository.getTransactionsForDateRange(sel.fromDate, sel.toDate),
            currencyPreferences.selectedCurrency,
        ) { domainTransactions, selectedCurrency ->
            val uiTransactions = domainTransactions.map { it.toUi() }
            convertTransactions(uiTransactions, selectedCurrency, sel.fromDate, sel.toDate)
        }
            .onEach { transactions ->
                _state.update {
                    it.copy(transactions = transactions, isLoading = false, isRefreshing = false)
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun convertTransactions(
        transactions: List<com.banko.app.ui.models.Transaction>,
        selectedCurrency: String,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): List<com.banko.app.ui.models.Transaction> {
        val currencyPairs = transactions
            .map { it.currency }
            .filter { it != selectedCurrency && isCurrencySupported(it) }
            .distinct()

        val ratesByCurrency = mutableMapOf<String, Map<LocalDate, Double>>()
        for (originalCurrency in currencyPairs) {
            val rates = currencyRepository.getRatesForDateRange(
                fromCurrency = originalCurrency,
                toCurrency = selectedCurrency,
                startDate = fromDate,
                endDate = toDate,
            )
            ratesByCurrency[originalCurrency] = rates
        }

        return transactions.map { tx ->
            if (tx.currency == selectedCurrency || !isCurrencySupported(tx.currency)) {
                tx
            } else {
                val rates = ratesByCurrency[tx.currency] ?: emptyMap()
                val rate = rates[tx.bookingDate.date]
                if (rate != null) {
                    tx.copy(amount = (tx.amount * rate * 100.0).roundToLong() / 100.0, currency = selectedCurrency)
                } else {
                    tx
                }
            }
        }
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
        } catch (e: Exception) {
            _state.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
        }
    }

    private fun handleSelectTag(tagId: String?) {
        _state.update {
            val newFilter: TagFilter = if (tagId == null) {
                if (it.tagFilter is TagFilter.Uncategorized) TagFilter.None
                else TagFilter.Uncategorized
            } else {
                if (it.tagFilter is TagFilter.ById && (it.tagFilter as TagFilter.ById).id == tagId) {
                    TagFilter.None
                } else {
                    TagFilter.ById(tagId)
                }
            }
            it.copy(tagFilter = newFilter)
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
                        tagFilter = TagFilter.None,
                    )
                }
            }
            is TimespanSelection.Year -> {
                _state.update {
                    it.copy(
                        selectedTimespan = timespan,
                        indicatorDateState = LocalDateTime(timespan.year, 1, 1, 0, 0),
                        isYearView = true,
                        tagFilter = TagFilter.None,
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
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMore = false, error = ErrorState(classifyError(e), e.message)) }
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
