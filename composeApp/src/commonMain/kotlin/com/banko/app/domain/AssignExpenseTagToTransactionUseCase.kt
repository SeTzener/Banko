package com.banko.app.domain

import com.banko.app.DatabaseExpenseTagRepository
import com.banko.app.DatabaseTransactionRepository

class AssignExpenseTagToTransactionUseCase(
    private val transactionRepository: DatabaseTransactionRepository,
    private val expenseTagRepository: DatabaseExpenseTagRepository
) {
    suspend operator fun invoke(transactionId: String, expenseTagId: String?) {
        val transaction = transactionRepository.findRawTransactionById(transactionId) ?: return
        expenseTagRepository.findExpenseTagById(expenseTagId).collect { tag ->
            transactionRepository.upsertTransaction(
                transaction = transaction.copy(expenseTagId = tag?.id),
                expenseTag = tag
            )
        }
    }
}