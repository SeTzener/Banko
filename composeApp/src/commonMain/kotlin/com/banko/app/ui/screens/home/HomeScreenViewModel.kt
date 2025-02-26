package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.ApiTransasctionRepository
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.api.dto.bankoApi.toModelItem
import com.banko.app.database.Entities.toModelItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class HomeScreenViewModel(
    private val dbRepository: DatabaseTransactionRepository,
    private val apiRepository: ApiTransasctionRepository
) : ViewModel() {
    private val _screenState = MutableStateFlow(HomeScreenState())
    val screenState: StateFlow<HomeScreenState> = _screenState

    init {
        loadData()
    }

    private fun loadData() {
        getDbTransactionsCount()
        observeTransactions(screenState.value.transactionsPageNumber * screenState.value.transactionsPageSize)
    }

    private fun observeTransactions(limit: Int) {
        viewModelScope.launch {
            dbRepository.getAllTransactions(limit).collect { transactions ->
                val observedTransactions = transactions.mapNotNull { it?.toModelItem() }
                _screenState.update {
                    it.copy(transactions = observedTransactions)
                }
            }
        }
    }

    private fun getDbTransactionsCount() {
        viewModelScope.launch {
            val count = dbRepository.getTransactionCount()
            _screenState.update { it.copy(dbTransactionsCount = count) }
        }
    }

    fun loadMoreTransactions(pageNumber: Int, pageSize: Int) {
        if (screenState.value.isLoading) return // Prevent multiple calls
        if (screenState.value.apiTransactionsCount <= pageNumber * pageSize && screenState.value.apiTransactionsCount != 0L) return

        _screenState.update { it.copy(isLoading = true) }

        try {

            if (screenState.value.dbTransactionsCount < pageNumber * pageSize) {
                loadNewTransactions(pageNumber, pageSize)
                getDbTransactionsCount()
                observeTransactions(limit = pageNumber * pageSize)
            } else {
                observeTransactions(limit = pageNumber * pageSize)
            }

            _screenState.update { it.copy(transactionsPageNumber = pageNumber) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _screenState.update { it.copy(isLoading = false) }
        }
    }


    fun loadNewTransactions(
        pageNumber: Int = screenState.value.transactionsPageNumber,
        pageSize: Int = screenState.value.transactionsPageSize
    ) {
        viewModelScope.launch {
            val result =
                apiRepository.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
            _screenState.update { it.copy(apiTransactionsCount = result.totalCount) }
            result.transactions.forEach {
                dbRepository.upsertTransaction(it.toModelItem())
            }
        }
    }
}