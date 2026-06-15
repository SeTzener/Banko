package com.banko.app.domain.repository

import com.banko.app.api.utils.Result
import com.banko.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface TransactionRepository {
    fun getTransactions(limit: Int): Flow<List<Transaction>>
    suspend fun fetchAndStoreTransactions(pageNumber: Int, pageSize: Int): Result<Long>
    suspend fun getStoredTransactionCount(): Long
    suspend fun getOldestTransactions(): LocalDateTime
    suspend fun deleteTransaction(transactionId: String)
}
