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

    fun loadData() {
        viewModelScope.launch {
            getTransactions()
        }
    }

    private suspend fun getTransactions() {
        dbRepository.getAllTransactions().collect { transactions ->
            val observedTransactions = transactions.mapNotNull { it?.toModelItem() }
            _screenState.update {
                it.copy(transactions = observedTransactions)
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            val result = apiRepository.getTransactions()
            result.transactions.forEach {
                dbRepository.upsertTransaction(it.toModelItem())
            }
        }
    }
}