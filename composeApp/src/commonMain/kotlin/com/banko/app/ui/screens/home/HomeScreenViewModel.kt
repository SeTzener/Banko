package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.ModelTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.banko.app.api.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val pageSize = 30

class HomeScreenViewModel(
    private val repository: DatabaseTransactionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private var currentPage = 0
    private var currentItems: List<ModelTransaction> = emptyList()
    private var activeFlowJobs = mutableListOf<Job>()

    init {
        loadInitialData()
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
                val job = repository.getLocalTransactions(limit = pageSize, offset = 0)
                    .onEach { transactions ->
                        _state.update {
                            it.copy(
                                transactions = transactions,
                                endReached = transactions.size < pageSize
                            )
                        }
                    }
                    .launchIn(this)

                activeFlowJobs.add(job)
                if (_state.value.totalTransactionCount == 0L) {
                    val result =
                        repository.fetchAndStoreTransactions(pageNumber = 1, pageSize = pageSize)
                    when (result) {
                        is Result.Success -> {
                            _state.update {
                                it.copy(totalTransactionCount = result.data, isLoading = false)
                            }
                        }

                        is Result.Error -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.error.name
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
                activeFlowJobs.clear()

                if (offset >= storedTransactionCount) {
                    if (_state.value.totalTransactionCount > storedTransactionCount) {
                        repository.fetchAndStoreTransactions(pageNumber = nextPage, pageSize = pageSize)
                    }
                }

                val job = repository.getLocalTransactions(limit = pageSize, offset = offset)
                    .onEach { newTransactions ->
                        currentItems = currentItems + newTransactions
                        _state.update {
                            it.copy(
                                transactions = currentItems,
                                isLoading = false,
                                endReached = newTransactions.isEmpty()
                            )
                        }
                        currentPage = nextPage
                    }.launchIn(this)

                activeFlowJobs.add(job)
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

    private fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            try {
                activeFlowJobs.forEach { it.cancel() }
                activeFlowJobs.clear()

                currentPage = 0

                val result =
                    repository.fetchAndStoreTransactions(pageNumber = 1, pageSize = pageSize)

                when (result) {
                    is Result.Success -> {
                        val job = repository.getLocalTransactions(limit = pageSize, offset = 0)
                            .onEach { transactions ->
                                _state.update {
                                    it.copy(
                                        transactions = transactions,
                                        isRefreshing = false,
                                        totalTransactionCount = result.data
                                    )
                                }
                            }
                            .launchIn(this)

                        activeFlowJobs.add(job)
                    }

                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.error.name
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
}