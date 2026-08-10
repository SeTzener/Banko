package com.banko.app.domain

import com.banko.app.data.repository.TransactionRepository

class SaveNoteUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: String, note: String) {
        transactionRepository.saveNote(id, note)
    }
}
