package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.ApiTransactionRepository
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.dto.bankoApi.toModelItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val pageSize = 30

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    private val dbRepository: DatabaseTransactionRepository,
    private val apiRepository: ApiTransactionRepository
) : ViewModel() {
    private val _screenState = MutableStateFlow(HomeScreenState())
    val screenState: StateFlow<HomeScreenState> = _screenState
    private val pageNumber = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            pageNumber
                .flatMapLatest { pageNumber ->
                    if (pageNumber != 0) {
                        observeTransactions(pageNumber)
                    } else {
                        observeTransactions(1)
                    }
                }
                .distinctUntilChanged()
                .collect { transactions ->
                    _screenState.update { state ->
                        state.copy(
                            transactions = transactions,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeTransactions(pageNumber: Int) = dbRepository.getAllTransactions(
        limit = pageNumber * pageSize
    )

    fun loadNewTransactions() {
        val newPageIncrement = pageNumber.value + 1
        if (screenState.value.isLoading) return
        if (screenState.value.dbTransactionsCount == screenState.value.apiTransactionsCount &&
            screenState.value.apiTransactionsCount <= screenState.value.transactions.size &&
            screenState.value.apiTransactionsCount > 0
        ) return

        _screenState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            if (screenState.value.dbTransactionsCount <= pageSize * newPageIncrement) {
                val transactions = fetchTransactions(newPageIncrement, pageSize)
                transactions.transactions.forEach {
                    dbRepository.upsertTransaction(it.toModelItem())
                }
                _screenState.update {
                    it.copy(
                        apiTransactionsCount = transactions.totalCount,
                    )
                }
            }
            val count = dbRepository.getTransactionCount()
            _screenState.update {
                it.copy(
                    dbTransactionsCount = count,
                    isLoading = false
                )
            }
            this@HomeScreenViewModel.pageNumber.value++
        }
    }

    fun getTransactionsToLoadCount(): Int = pageNumber.value * pageSize

    private suspend fun fetchTransactions(pageNumber: Int, pageSize: Int): Transactions =
        apiRepository.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
}