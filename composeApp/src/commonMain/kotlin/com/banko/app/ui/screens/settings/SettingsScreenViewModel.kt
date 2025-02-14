package com.banko.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.dto.bankoApi.ExpenseTag
import com.banko.app.api.repositories.ExpenseTagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenViewModel: ViewModel() {
    private val repository = ExpenseTagRepository()
    private val _screenState = MutableStateFlow(SettingsScreenState())
    val screenState: StateFlow<SettingsScreenState> = _screenState

    fun getExpenseTags() {
        viewModelScope.launch {
            val result = repository.getExpenseTags()
            _screenState.update { it.copy(expenseTags = result) }
        }
    }

    fun updateExpenseTag(expenseTag: ExpenseTag) {
        viewModelScope.launch {
            val result = repository.updateExpenseTag(expenseTag)
            if (result != null) {
                _screenState.update { it.copy(expenseTags = it.expenseTags.map { tag -> if (tag.id == result.id) result else tag }) }
            }
        }
    }

    fun createExpenseTag(name: String, color: Long) {
        viewModelScope.launch {
            val result = repository.createExpenseTag(name, color)
            if (result != null) {
                _screenState.update { it.copy(expenseTags = it.expenseTags + result) }
            }
        }
    }

    fun deleteExpenseTag(expenseTagId: String) {
        viewModelScope.launch {
            repository.deleteExpenseTag(expenseTagId)
            _screenState.update { it.copy(expenseTags = it.expenseTags.filter { tag -> tag.id != expenseTagId }) }
        }
    }
}