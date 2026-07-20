package com.banko.app.domain

import com.banko.app.DatabaseExpenseTagRepository
import com.banko.app.DatabaseTransactionRepository
import kotlinx.coroutines.flow.first

class AssignExpenseTagToTransactionUseCase(
    private val transactionRepository: DatabaseTransactionRepository,
    private val expenseTagRepository: DatabaseExpenseTagRepository
) {
    suspend operator fun invoke(transactionId: String, expenseTagId: String?) {
        val transaction = transactionRepository.findRawTransactionById(transactionId)
            ?: error("no transaction in db with id $transactionId")
        if (expenseTagId == null) {
            transactionRepository.upsertTransaction(
                transaction = transaction.copy(expenseTagId = null),
                expenseTag = null
            )
            return
        }
        val tag = expenseTagRepository.findExpenseTagById(expenseTagId).first()
            ?: error("no expense tag found in db with id $expenseTagId")
        transactionRepository.upsertTransaction(
            transaction = transaction.copy(expenseTagId = tag.id),
            expenseTag = tag
        )
    }
}