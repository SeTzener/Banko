package com.banko.app.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.ApiExpenseTagRepository
import com.banko.app.DatabaseExpenseTagRepository
import com.banko.app.database.Entities.toModelItem
import com.banko.app.domain.AssignExpenseTagToTransactionUseCase
import com.banko.app.domain.GetAllExpenseTagUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsScreenViewModel(
    private val apiRepository: ApiExpenseTagRepository,
    private val updateTransactionUseCase: AssignExpenseTagToTransactionUseCase,
    private val getExpenseTags: GetAllExpenseTagUseCase,
) : ViewModel() {
    private val _screenState = MutableStateFlow(DetailScreenState())
    val screenState: StateFlow<DetailScreenState> = _screenState

    init {
        getExpenseTags()
    }

    fun getExpenseTags() {
        viewModelScope.launch {
            getExpenseTags.invoke().collect { result ->
                _screenState.update { it.copy(expenseTags = result.mapNotNull { it?.toModelItem() }) }
            }
        }
    }

    fun assignExpenseTag(id: String, expenseTagId: String?) {
        viewModelScope.launch {
            apiRepository.assignExpenseTag(id, expenseTagId)
            updateTransactionUseCase.invoke(transactionId = id, expenseTagId =  expenseTagId)
        }
    }
}