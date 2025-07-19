package com.banko.app.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.ApiExpenseTagRepository
import com.banko.app.database.Entities.toModelItem
import com.banko.app.domain.AssignExpenseTagToTransactionUseCase
import com.banko.app.domain.GetAllExpenseTagUseCase
import com.banko.app.domain.SaveNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsScreenViewModel(
    private val apiTagRepository: ApiExpenseTagRepository,
    private val updateTransactionUseCase: AssignExpenseTagToTransactionUseCase,
    private val getExpenseTags: GetAllExpenseTagUseCase,
    private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {
    private val _screenState = MutableStateFlow(DetailScreenState())
    val screenState: StateFlow<DetailScreenState> = _screenState

    private val pendingUpdates = mutableMapOf<String, String?>()

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
        pendingUpdates[id] = expenseTagId
        viewModelScope.launch {
            try {
                apiTagRepository.assignExpenseTag(id, expenseTagId)
                updateTransactionUseCase.invoke(transactionId = id, expenseTagId = expenseTagId)
                pendingUpdates.remove(id)

            } catch(e: Exception) {
                pendingUpdates[id]?.let { oldTagId ->
                    updateTransactionUseCase.invoke(id, oldTagId)
                }
                pendingUpdates.remove(id)
            }
        }
    }

    fun saveNote(text: String, id: String) {
        viewModelScope.launch {
            try {
                saveNoteUseCase.invoke(id = id, note = text)
            } catch (ex: Exception) {
                screenState.value.copy(
                    error = ex.message
                )
            }
        }
    }
}