package com.banko.app.domain.repository

import com.banko.app.api.utils.Result
import com.banko.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

import kotlinx.datetime.LocalDate

interface TransactionRepository {
    fun getTransactions(limit: Int): Flow<List<Transaction>>
    suspend fun fetchAndStoreTransactions(pageNumber: Int, pageSize: Int): Result<Long>
    suspend fun getStoredTransactionCount(): Long
    suspend fun getOldestTransactions(): LocalDateTime
    suspend fun deleteTransaction(transactionId: String)
    fun getTransactionsForDateRange(fromDate: LocalDate, toDate: LocalDate): Flow<List<Transaction>>
    suspend fun fetchAndStoreTransactionsForDateRange(fromDate: LocalDate, toDate: LocalDate)
}
