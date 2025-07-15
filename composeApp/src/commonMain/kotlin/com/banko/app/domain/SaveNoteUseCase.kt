package com.banko.app.domain

import com.banko.app.ApiTransactionRepository
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.api.utils.Result

class SaveNoteUseCase(
    private val apiTransactionsRepository: ApiTransactionRepository,
    private val transactionRepository: DatabaseTransactionRepository
) {
    suspend operator fun invoke(id: String, note: String) {
        try {
            val apiResult = apiTransactionsRepository.saveNote(id = id, text = note)
            if (apiResult is Result.Success)  {
                transactionRepository.saveNote(id, note)
            }
        } catch (ex: Exception){
            println(ex)
        }
    }
}