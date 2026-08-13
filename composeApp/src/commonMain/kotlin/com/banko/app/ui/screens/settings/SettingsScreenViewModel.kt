package com.banko.app.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import com.banko.app.data.repository.ExpenseTagRepository
import com.banko.app.domain.CurrencyPreferences
import com.banko.app.domain.model.getSupportedCurrencies
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.models.toDomain
import com.banko.app.ui.models.toUi
import com.banko.app.ui.utils.ErrorState
import com.banko.app.ui.utils.classifyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val expenseTagRepository: ExpenseTagRepository,
    private val currencyPreferences: CurrencyPreferences,
    private val apiService: BankoApiService,
) : ViewModel() {
    private val _screenState = MutableStateFlow(SettingsScreenState())
    val screenState: StateFlow<SettingsScreenState> = _screenState

    init {
        getExpenseTags()
        loadCurrencyPreferences()
        loadBankAuthorizations()
    }

    private fun getExpenseTags() {
        viewModelScope.launch {
            expenseTagRepository.getAllExpenseTags()
                .collect { tags ->
                    _screenState.update { it.copy(expenseTags = tags.map { tag -> tag.toUi() }) }
                }
        }
    }

    private fun loadCurrencyPreferences() {
        viewModelScope.launch {
            currencyPreferences.selectedCurrency.collect { code ->
                val currencies = getSupportedCurrencies()
                val selected = currencies.find { it.code == code } ?: currencies.first()
                _screenState.update {
                    it.copy(
                        selectedCurrency = selected,
                        availableCurrencies = currencies
                    )
                }
            }
        }
    }

    fun setCurrency(code: String) {
        viewModelScope.launch {
            currencyPreferences.setSelectedCurrency(code)
        }
    }

    fun loadExpenseTags() {
        viewModelScope.launch {
            try {
                expenseTagRepository.refreshExpenseTags()
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun updateExpenseTag(expenseTag: ExpenseTag) {
        viewModelScope.launch {
            try {
                expenseTagRepository.updateExpenseTag(expenseTag.toDomain())
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun createExpenseTag(name: String, color: Color, isEarning: Boolean) {
        viewModelScope.launch {
            try {
                expenseTagRepository.createExpenseTag(name, color.toArgb().toLong(), isEarning)
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun deleteExpenseTag(expenseTagId: String) {
        viewModelScope.launch {
            try {
                expenseTagRepository.deleteExpenseTag(expenseTagId)
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun clearError() {
        _screenState.update { it.copy(error = null) }
    }

    fun loadBankAuthorizations() {
        viewModelScope.launch {
            _screenState.update { it.copy(isLoadingBanks = true) }
            when (val result = apiService.getBankAuthorizations()) {
                is Result.Success -> {
                    _screenState.update {
                        it.copy(
                            bankAuthorizations = result.value.bankAuthorizations,
                            isLoadingBanks = false,
                        )
                    }
                }
                is Result.Error -> {
                    _screenState.update {
                        it.copy(isLoadingBanks = false)
                    }
                }
            }
        }
    }
}
