package com.banko.app.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.dto.bankoApi.toModelItem
import com.banko.app.api.services.BankoApiService
import kotlinx.coroutines.launch
import com.banko.app.api.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class HomeScreenViewModel : ViewModel() {
    private val apiService = BankoApiService()
    private val _screenState = MutableStateFlow(HomeScreenState())
    val screenState: StateFlow<HomeScreenState> = _screenState

    // TODO() Add a refresh logic
    fun loadData() {
        if (screenState.value.isLoading || screenState.value.transactions.isNotEmpty()) return

        _screenState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getTransactions()
        }
    }

    private suspend fun getTransactions() {
        try {
            val result = apiService.getTransactions()
            if (result is Result.Success) {
                val transactionList = result.data.transactions.map { it.toModelItem() }
                _screenState.update { it.copy(transactions = transactionList) }
            } else if (result is Result.Error) {
                throw Exception(result.error.name)
            }
        } catch (e: Exception) {
            println("Error fetching data: ${e.message}")
        } finally {
            _screenState.update { it.copy(isLoading = false) }
        }
    }
}