package com.banko.app.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.data.repository.TransactionRepository
import com.banko.app.domain.AssignExpenseTagToTransactionUseCase
import com.banko.app.domain.GetAllExpenseTagUseCase
import com.banko.app.domain.SaveNoteUseCase
import com.banko.app.ui.models.toUi
import com.banko.app.ui.utils.ErrorState
import com.banko.app.ui.utils.classifyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsScreenViewModel(
    private val updateTransactionUseCase: AssignExpenseTagToTransactionUseCase,
    private val getExpenseTags: GetAllExpenseTagUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val _screenState = MutableStateFlow(DetailScreenState())
    val screenState: StateFlow<DetailScreenState> = _screenState

    init {
        getExpenseTags()
    }

    fun getExpenseTags() {
        viewModelScope.launch {
            getExpenseTags.invoke().collect { result ->
                _screenState.update { it.copy(expenseTags = result.map { it.toUi() }) }
            }
        }
    }

    fun assignExpenseTag(id: String, expenseTagId: String?) {
        viewModelScope.launch {
            try {
                updateTransactionUseCase.invoke(transactionId = id, expenseTagId = expenseTagId)
                _screenState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(e), e.message)) }
            }
        }
    }

    fun saveNote(text: String, id: String) {
        viewModelScope.launch {
            try {
                saveNoteUseCase.invoke(id = id, note = text)
                _screenState.update { it.copy(error = null) }
            } catch (ex: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(ex), ex.message)) }
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transactionId)
                _screenState.update { it.copy(error = null) }
            } catch (ex: Exception) {
                _screenState.update { it.copy(error = ErrorState(classifyError(ex), ex.message)) }
            }
        }
    }

    fun clearError() {
        _screenState.update { it.copy(error = null) }
    }
}