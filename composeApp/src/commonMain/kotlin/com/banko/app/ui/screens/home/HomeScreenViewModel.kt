package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.DatabaseTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.banko.app.api.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDateTime

private const val pageSize = 30

class HomeScreenViewModel(
    private val repository: DatabaseTransactionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private var currentPage = 0
    private var activeFlowJobs = mutableListOf<Job>()

    init {
        loadInitialData()
        observeIndicatorDateChanges()
        getOldestTransactionDate()
    }

    override fun onCleared() {
        super.onCleared()
        activeFlowJobs.forEach { it.cancel() }
    }

    private fun loadInitialData() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val job = repository.getLocalTransactions(limit = pageSize)
                    .onEach { transactions ->
                        _state.update {
                            it.copy(
                                transactions = transactions,
                                endReached = transactions.size < pageSize,
                            )
                        }
                    }
                    .launchIn(this)

                val monthlyTransactions = repository.getTransactionsForMonth(
                    month = _state.value.indicatorDateState,
                    year = _state.value.indicatorDateState.year
                ).onEach { transactions ->
                    _state.update {
                        it.copy(
                            monthlyTransactions = transactions
                        )
                    }
                }.launchIn(this)

                activeFlowJobs.addAll(
                    listOf(
                        job,
                        monthlyTransactions,
                    )
                )
                if (_state.value.totalTransactionCount == 0L) {
                    val result =
                        repository.fetchAndStoreTransactions(pageNumber = 1, pageSize = pageSize)
                    when (result) {
                        is Result.Success<Long> -> {
                            _state.update {
                                it.copy(totalTransactionCount = result.value, isLoading = false)
                            }
                        }

                        is Result.Error.HttpError -> {
                            println(result.fullErrorMessage())
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.errorMessageToDisplay()
                                )
                            }
                        }

                        is Result.Error.NetworkError -> {
                            println(result.exception.message)
                            _state.update {
                                it.copy(
                                    isRefreshing = false,
                                    error = result.exception.message
                                )
                            }
                        }
                        is Result.Error.SerializationError -> {
                            println(result.exception.message)
                            _state.update {
                                it.copy(
                                    isRefreshing = false,
                                    error = result.exception.message
                                )
                            }
                        }
                        is Result.Error.UnexpectedError -> {
                            println(result.exception.message)
                            _state.update {
                                it.copy(
                                    isRefreshing = false,
                                    error = result.exception.message
                                )
                            }
                        }
                    }

                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transactions"
                    )
                }
            } finally {
                currentPage = 1
            }
        }
    }

    private fun observeIndicatorDateChanges() {
        viewModelScope.launch {
            state
                .map { it.indicatorDateState }
                .distinctUntilChanged()
                .collect { date ->
                    loadMonthlyTransactions(date)
                }
        }
    }

    private fun loadMonthlyTransactions(date: LocalDateTime) {
        activeFlowJobs.removeAll { it.isCompleted }

        val job = repository.getTransactionsForMonth(
            month = date,
            year = date.year
        ).onEach { transactions ->
            _state.update {
                it.copy(monthlyTransactions = transactions)
            }
        }.launchIn(viewModelScope)

        activeFlowJobs.add(job)
    }

    fun handleEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.LoadMore -> loadMoreData()
            is TransactionsEvent.Refresh -> refreshData()
            is TransactionsEvent.ErrorShown -> clearError(event.error)
        }
    }

    private fun loadMoreData() {
        if (_state.value.isLoading || _state.value.endReached) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val nextPage = currentPage + 1
                val offset = nextPage * pageSize
                val storedTransactionCount = repository.getStoredTransactionCount()

                activeFlowJobs.forEach { it.cancel() }

                if (storedTransactionCount < _state.value.totalTransactionCount) {
                    if (offset >= storedTransactionCount) {
                        val result = repository.fetchAndStoreTransactions(
                            pageNumber = nextPage,
                            pageSize = pageSize
                        )
                        when (result) {
                            is Result.Success -> {
                                loadLocalTransactions(nextPage, offset, this)
                            }

                            is Result.Error.HttpError -> _state.update {
                                println(result.fullErrorMessage())
                                it.copy(
                                    isLoading = false,
                                    error = result.errorMessageToDisplay()
                                )
                            }

                            is Result.Error.NetworkError -> {
                                println(result.exception.message)
                                _state.update {
                                    it.copy(
                                        isRefreshing = false,
                                        error = result.exception.message
                                    )
                                }
                            }
                            is Result.Error.SerializationError -> {
                                println(result.exception.message)
                                _state.update {
                                    it.copy(
                                        isRefreshing = false,
                                        error = result.exception.message
                                    )
                                }
                            }
                            is Result.Error.UnexpectedError -> {
                                println(result.exception.message)
                                _state.update {
                                    it.copy(
                                        isRefreshing = false,
                                        error = result.exception.message
                                    )
                                }
                            }
                        }
                    } else {
                        loadLocalTransactions(nextPage, offset, this)
                    }
                } else {
                    if (_state.value.transactions.count() < storedTransactionCount) {
                        loadLocalTransactions(nextPage, offset, this)
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                endReached = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load more transactions"
                    )
                }
            }
        }
    }

    private fun getOldestTransactionDate() {
        viewModelScope.launch {
            val date = repository.getOldestTransactions()
            _state.update { it.copy(oldestTransactionDate = date) }
        }
    }

    private fun loadLocalTransactions(nextPage: Int, offset: Int, scope: CoroutineScope) {
        val job = repository.getLocalTransactions(limit = offset)
            .onEach { newTransactions ->
                _state.update {
                    it.copy(
                        transactions = newTransactions,
                        isRefreshing = false,
                        isLoading = false,
                        endReached = newTransactions.isEmpty()
                    )
                }
                currentPage = nextPage
            }.launchIn(scope)

        activeFlowJobs.add(job)
    }

    private fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            try {
                activeFlowJobs.forEach { it.cancel() }
                activeFlowJobs.clear()

                val result =
                    repository.fetchAndStoreTransactions(pageNumber = 1, pageSize = pageSize)

                when (result) {
                    is Result.Success -> {
                        loadLocalTransactions(currentPage, pageSize, this)
                    }

                    is Result.Error.HttpError -> {
                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.errorMessageToDisplay()
                            )
                        }
                    }

                    is Result.Error.NetworkError -> {
                        println(result.exception.message)
                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.exception.message
                            )
                        }
                    }
                    is Result.Error.SerializationError -> {
                        println(result.exception.message)
                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.exception.message
                            )
                        }
                    }
                    is Result.Error.UnexpectedError -> {
                        println(result.exception.message)
                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.exception.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        error = e.message ?: "Failed to refresh transactions"
                    )
                }
            }
        }
    }

    private fun clearError(error: String) {
        if (_state.value.error == error) {
            _state.update { it.copy(error = null) }
        }
    }

    fun indicatorDatePicker(date: LocalDateTime) {
        _state.update { it.copy(indicatorDateState = date) }
    }
}
