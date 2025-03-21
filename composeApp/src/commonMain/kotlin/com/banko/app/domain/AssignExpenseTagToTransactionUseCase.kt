package com.banko.app.domain

import com.banko.app.DatabaseExpenseTagRepository
import com.banko.app.DatabaseTransactionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

class AssignExpenseTagToTransactionUseCase(
    private val transactionRepository: DatabaseTransactionRepository,
    private val expenseTagRepository: DatabaseExpenseTagRepository
) {
    suspend operator fun invoke(transactionId: String, expenseTagId: String?) {
        var transaction = transactionRepository.findRawTransactionById(transactionId) ?: return
        expenseTagRepository.findExpenseTagById(expenseTagId).collect { tag ->
            transactionRepository.upsertTransaction(
                transaction = transaction.copy(expenseTagId = tag?.id),
                expenseTag = tag
            )
        }
    }
}