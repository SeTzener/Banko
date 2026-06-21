package com.banko.app.domain

import com.banko.app.ApiTransactionRepository
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.api.utils.Result

class SaveNoteUseCase(
    private val apiTransactionsRepository: ApiTransactionRepository,
    private val transactionRepository: DatabaseTransactionRepository
) {
    suspend operator fun invoke(id: String, note: String) {
        val apiResult = apiTransactionsRepository.saveNote(id = id, text = note)
        when (apiResult) {
            is Result.Error -> throw RuntimeException("Failed to save note: $apiResult")
            is Result.Success -> transactionRepository.saveNote(id, note)
        }
    }
}