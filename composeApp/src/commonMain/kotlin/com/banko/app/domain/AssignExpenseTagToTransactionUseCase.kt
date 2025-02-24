package com.banko.app.domain

import com.banko.app.DatabaseExpenseTagRepository
import com.banko.app.DatabaseTransactionRepository

class AssignExpenseTagToTransactionUseCase(
    private val transactionRepository: DatabaseTransactionRepository,
    private val expenseTagRepository: DatabaseExpenseTagRepository
) {
    suspend operator fun invoke(transactionId: String, expenseTagId: String?) {
        val tag = expenseTagRepository.findExpenseTagById(expenseTagId)
        var transaction = transactionRepository.findRawTransactionById(transactionId) ?: return

        transactionRepository.upsertTransaction(
            transaction = transaction.copy(expenseTagId = tag?.id),
            expenseTag = tag
        )
    }
}