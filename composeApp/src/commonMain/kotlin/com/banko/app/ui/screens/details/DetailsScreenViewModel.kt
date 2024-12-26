package com.banko.app.ui.screens.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.HttpClientProvider
import com.banko.app.api.services.NordigenApiService
import kotlinx.coroutines.launch

// TODO(): Temporary, change it to actual implementation
class DetailsScreenViewModel : ViewModel() {
    private val apiService = NordigenApiService(HttpClientProvider.client)
    var screenState: DetailScreenState by mutableStateOf(DetailScreenState())

    init {
        fetchExampleData()
    }

    private fun fetchExampleData() {
        viewModelScope.launch {
            try {
                val data = apiService.getExampleData()
                screenState = screenState.copy(data = data.fact)
            } catch (e: Exception) {
                println("Error fetching data: ${e.message}")
            }
        }
    }
}