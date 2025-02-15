package com.banko.app.ui.screens.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.services.NordigenApiService
import com.banko.app.api.utils.Result
import kotlinx.coroutines.launch

// TODO(): Temporary, change it to actual implementation
class DetailsScreenViewModel : ViewModel() {
    // TODO(): create a new client for every service. This one is static

    private val apiService = NordigenApiService()
    var screenState: DetailScreenState by mutableStateOf(DetailScreenState())

    private fun fetchExampleData() {
        viewModelScope.launch {
            try {
                val result = apiService.getInstitution("IT")
                if (result is Result.Success) {
                    screenState =
                        screenState.copy(data = result.data[12].name)
                } else if (result is Result.Error){
                    throw Exception(result.error.name)
                }
            } catch (e: Exception) {
                println("Error fetching data: ${e.message}")
            }
        }
    }
}