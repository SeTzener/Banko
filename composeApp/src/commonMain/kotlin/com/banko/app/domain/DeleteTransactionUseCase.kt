package com.banko.app.domain

import com.banko.app.ApiTransactionRepository
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.api.utils.Result

class DeleteTransactionUseCase(
    private val apiTransactionsRepository: ApiTransactionRepository,
    private val transactionRepository: DatabaseTransactionRepository
) {
    suspend operator fun invoke(transactionId: String) {
        try {
            val apiResult = apiTransactionsRepository.deleteTransaction(transactionId)
            if (apiResult is Result.Success) {
                transactionRepository.deleteTransaction(transactionId)
            }
        } catch (ex: Exception) {
            println(ex)
        }
    }
}