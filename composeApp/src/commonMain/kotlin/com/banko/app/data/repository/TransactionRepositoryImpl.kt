package com.banko.app.data.repository

import com.banko.app.api.utils.Result
import com.banko.app.data.local.TransactionLocalDataSource
import com.banko.app.data.remote.TransactionRemoteDataSource
import com.banko.app.domain.model.Transaction
import com.banko.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

class TransactionRepositoryImpl(
    private val local: TransactionLocalDataSource,
    private val remote: TransactionRemoteDataSource
) : TransactionRepository {

    override fun getTransactions(limit: Int): Flow<List<Transaction>> =
        local.getTransactions(limit)

    override suspend fun fetchAndStoreTransactions(pageNumber: Int, pageSize: Int): Result<Long> {
        val result = remote.fetchTransactions(pageNumber, pageSize)
        return when (result) {
            is Result.Error -> result
            is Result.Success -> {
                result.value.transactions.forEach { local.upsertTransaction(it) }
                Result.Success(result.value.totalCount)
            }
        }
    }

    override suspend fun getStoredTransactionCount(): Long =
        local.getStoredTransactionCount()

    override suspend fun getOldestTransactions(): LocalDateTime =
        local.getOldestTransactions()

    override suspend fun deleteTransaction(transactionId: String) {
        val apiResult = remote.deleteTransaction(transactionId)
        if (apiResult is Result.Success) {
            local.deleteTransaction(transactionId)
        }
    }
}
