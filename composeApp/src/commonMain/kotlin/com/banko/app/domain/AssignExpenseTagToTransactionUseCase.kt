package com.banko.app.domain

import com.banko.app.DatabaseExpenseTagRepository
import com.banko.app.DatabaseTransactionRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class AssignExpenseTagToTransactionUseCase(
    private val transactionRepository: DatabaseTransactionRepository,
    private val expenseTagRepository: DatabaseExpenseTagRepository
) {
    suspend operator fun invoke(transactionId: String, expenseTagId: String?) {
        val transaction = transactionRepository.findRawTransactionById(transactionId)
            ?: error("no transaction in db with id $transactionId")
        val tag = expenseTagRepository.findExpenseTagById(expenseTagId).first()
            ?: error("no expense tag found in db with id $expenseTagId")
        transactionRepository.upsertTransaction(
            transaction = transaction.copy(expenseTagId = tag.id),
            expenseTag = tag
        )
    }
}