package com.banko.app.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.repositories.ExpenseTagRepository
import com.banko.app.ui.models.ExpenseTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsScreenViewModel : ViewModel() {
    private val repository = ExpenseTagRepository()
    private val _screenState = MutableStateFlow(DetailScreenState())
    val screenState: StateFlow<DetailScreenState> = _screenState

    fun getExpenseTags() {
        viewModelScope.launch {
            val result = repository.getExpenseTags()
            _screenState.update { it.copy(expenseTags = result) }
        }
    }

    fun assignExpenseTag(id: String, expenseTagId: String?) {
        viewModelScope.launch {
            repository.assignExpenseTag(id, expenseTagId)
        }
    }
}