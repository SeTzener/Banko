package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.ApiTransactionRepository
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.dto.bankoApi.toModelItem
import com.banko.app.database.Entities.toModelItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val dbRepository: DatabaseTransactionRepository,
    private val apiRepository: ApiTransactionRepository
) : ViewModel() {
    private val _screenState = MutableStateFlow(HomeScreenState())
    val screenState: StateFlow<HomeScreenState> = _screenState

     fun observeTransactions(pageNumber: Int, pageSize: Int) {
        viewModelScope.launch {
            dbRepository.getAllTransactions(
                pageNumber = pageNumber,
                pageSize = pageSize
            ).collect { transactions ->
                _screenState.update { it ->
                    it.copy(
                        transactions = (screenState.value.transactions + transactions.map { it.toModelItem() })
                            .sortedByDescending { it.bookingDate }
                            .distinctBy { it.id }
                    )
                }
            }
        }
    }

    fun loadNewTransactions(pageNumber: Int, pageSize: Int) {
        if (screenState.value.isLoading) return
        if (screenState.value.dbTransactionsCount == screenState.value.apiTransactionsCount && screenState.value.apiTransactionsCount > 0) return

        _screenState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            if (screenState.value.dbTransactionsCount < pageSize * pageNumber) {
                val transactions = fetchTransactions(pageNumber, pageSize)
                transactions.transactions.forEach {
                    dbRepository.upsertTransaction(it.toModelItem())
                }
                _screenState.update {
                    it.copy(
                        apiTransactionsCount = transactions.totalCount,
                    )}
            }
            val count = dbRepository.getTransactionCount()
            _screenState.update {
                it.copy(
                    transactionsPageNumber = pageNumber,
                    dbTransactionsCount = count,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun fetchTransactions(pageNumber: Int, pageSize: Int): Transactions =
        apiRepository.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
}