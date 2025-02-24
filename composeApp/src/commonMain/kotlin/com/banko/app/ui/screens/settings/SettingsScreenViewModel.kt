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
            val result = apiRepository.getExpenseTags()
            result.forEach {
                dbRepository.upsertExpenseTag(it.toDao())
            }
        }
    }

    fun updateExpenseTag(expenseTag: ExpenseTag) {
        viewModelScope.launch {
            val result = apiRepository.updateExpenseTag(expenseTag) ?: return@launch
            dbRepository.upsertExpenseTag(result.toDao())
        }
    }

    fun createExpenseTag(name: String, color: Color) {
        viewModelScope.launch {
            val result =
                apiRepository.createExpenseTag(name, color.toArgb().toLong()) ?: return@launch
            dbRepository.upsertExpenseTag(result.toDao())
        }
    }

    fun deleteExpenseTag(expenseTagId: String) {
        viewModelScope.launch {
            apiRepository.deleteExpenseTag(expenseTagId) ?: return@launch
            dbRepository.deleteExpenseTag(expenseTagId)
        }
    }
}