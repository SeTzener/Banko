package com.banko.app.data.repository

import com.banko.app.api.utils.Result
import com.banko.app.data.local.TransactionLocalDataSource
import com.banko.app.data.remote.TransactionRemoteDataSource
import com.banko.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class TransactionRepository(
    private val local: TransactionLocalDataSource,
    private val remote: TransactionRemoteDataSource
) {

    fun getTransactions(limit: Int): Flow<List<Transaction>> =
        local.getTransactions(limit)

    suspend fun fetchAndStoreTransactions(pageNumber: Int, pageSize: Int): Result<Long> {
        val result = remote.fetchTransactions(pageNumber, pageSize)
        return when (result) {
            is Result.Error -> result
            is Result.Success -> {
                result.value.transactions.forEach { local.upsertTransaction(it) }
                Result.Success(result.value.totalCount)
            }
        }
    }

    suspend fun getStoredTransactionCount(): Long =
        local.getStoredTransactionCount()

    suspend fun getOldestTransactions(): LocalDateTime =
        local.getOldestTransactions()

    fun getTransactionsForDateRange(fromDate: LocalDate, toDate: LocalDate): Flow<List<Transaction>> =
        local.getTransactionsForDateRange(fromDate, toDate)

    suspend fun fetchAndStoreTransactionsForDateRange(fromDate: LocalDate, toDate: LocalDate) {
        val result = remote.fetchTransactionsForDateRange(fromDate, toDate)
        if (result is Result.Success) {
            result.value.transactions.forEach { local.upsertTransaction(it) }
        }
    }

    suspend fun getTransactionById(transactionId: String): Transaction? =
        local.getTransactionById(transactionId)

    suspend fun deleteTransaction(transactionId: String) {
        val apiResult = remote.deleteTransaction(transactionId)
        if (apiResult is Result.Success) {
            local.deleteTransaction(transactionId)
        }
    }
}
