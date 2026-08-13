package com.banko.app.domain

import com.banko.app.data.repository.ExpenseTagRepository
import com.banko.app.data.repository.TransactionRepository

class AssignExpenseTagToTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val expenseTagRepository: ExpenseTagRepository
) {
    suspend operator fun invoke(transactionId: String, expenseTagId: String?) {
        val transaction = transactionRepository.getTransactionById(transactionId)
            ?: error("no transaction in db with id $transactionId")
        if (expenseTagId != null) {
            expenseTagRepository.getExpenseTagById(expenseTagId)
                ?: error("no expense tag found in db with id $expenseTagId")
        }
        transactionRepository.assignExpenseTag(transactionId, expenseTagId)
    }
}
