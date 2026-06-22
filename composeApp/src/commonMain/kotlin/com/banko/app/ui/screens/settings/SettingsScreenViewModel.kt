package com.banko.app.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.ApiExpenseTagRepository
import com.banko.app.DatabaseExpenseTagRepository
import com.banko.app.database.Entities.toModelItem
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.models.toDao
import com.banko.app.ui.utils.ErrorState
import com.banko.app.ui.utils.classifyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val dbRepository: DatabaseExpenseTagRepository,
    private val apiRepository: ApiExpenseTagRepository
) : ViewModel() {
    private val _screenState = MutableStateFlow(SettingsScreenState())
    val screenState: StateFlow<SettingsScreenState> = _screenState

    init {
        getExpenseTags()
    }

    private fun getExpenseTags() {
        viewModelScope.launch {
            dbRepository.getAllExpenseTags()
                .collect { result ->
                    _screenState.update { it.copy(expenseTags = result.mapNotNull { it?.toModelItem() }) }
                }
        }
    }

    fun loadExpenseTags() {
        viewModelScope.launch {
            try {
                val result = apiRepository.getExpenseTags()
                result.forEach {
                    dbRepository.upsertExpenseTag(it.toDao())
                }
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun updateExpenseTag(expenseTag: ExpenseTag) {
        viewModelScope.launch {
            try {
                val result = apiRepository.updateExpenseTag(expenseTag)
                dbRepository.upsertExpenseTag(result.toDao())
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun createExpenseTag(name: String, color: Color, isEarning: Boolean) {
        viewModelScope.launch {
            try {
                val result =
                    apiRepository.createExpenseTag(name, color.toArgb().toLong(), isEarning)
                dbRepository.upsertExpenseTag(result.toDao())
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun deleteExpenseTag(expenseTagId: String) {
        viewModelScope.launch {
            try {
                apiRepository.deleteExpenseTag(expenseTagId)
                dbRepository.deleteExpenseTag(expenseTagId)
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun clearError() {
        _screenState.update { it.copy(error = null) }
    }
}