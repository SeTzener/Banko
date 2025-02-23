package com.banko.app.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.database.Entities.toModel
import com.banko.app.database.repository.ExpenseTagRepository as DbExpenseTagRepository
import com.banko.app.api.repositories.ExpenseTagRepository as ApiExpenseTagRepository
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.models.toDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val dbRepository: DbExpenseTagRepository
) : ViewModel() {
    private val apiRepository = ApiExpenseTagRepository()
    private val _screenState = MutableStateFlow(SettingsScreenState())
    val screenState: StateFlow<SettingsScreenState> = _screenState

    init {
        loadExpenseTags()
    }

    fun getExpenseTags() {
        viewModelScope.launch {
            dbRepository.getAllExpenseTags()
                .collect { result ->
                    _screenState.update { it.copy(expenseTags = result.map { it!!.toModel() }) }
                }
        }
    }

    fun loadExpenseTags() {
        viewModelScope.launch {
            val result = apiRepository.getExpenseTags()
            result.forEach {
                dbRepository.upsertExpenseTag(it.toDao())
            }
            _screenState.update { it.copy(expenseTags = result) }
        }
    }

    fun updateExpenseTag(expenseTag: ExpenseTag) {
        viewModelScope.launch {
            val result = apiRepository.updateExpenseTag(expenseTag)
            if (result != null) {
                _screenState.update { it.copy(expenseTags = it.expenseTags.map { tag -> if (tag.id == result.id) result else tag }) }
            }
        }
    }

    fun createExpenseTag(name: String, color: Color) {
        viewModelScope.launch {
            val result = apiRepository.createExpenseTag(name, color.toArgb().toLong())
            if (result != null) {
                dbRepository.upsertExpenseTag(result.toDao())
                _screenState.update { it.copy(expenseTags = it.expenseTags + result) }
            }
        }
    }

    fun deleteExpenseTag(expenseTagId: String) {
        viewModelScope.launch {
            apiRepository.deleteExpenseTag(expenseTagId)
            _screenState.update { it.copy(expenseTags = it.expenseTags.filter { tag -> tag.id != expenseTagId }) }
        }
    }
}