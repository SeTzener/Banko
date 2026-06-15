package com.banko.app.data.remote

import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import com.banko.app.data.mapper.toDomain
import com.banko.app.domain.model.Transaction

class TransactionRemoteDataSource(
    private val apiService: BankoApiService
) {
    data class FetchResult(
        val transactions: List<Transaction>,
        val totalCount: Long
    )

    suspend fun fetchTransactions(pageNumber: Int, pageSize: Int): Result<FetchResult> {
        val result = apiService.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
        return when (result) {
            is Result.Error -> result
            is Result.Success -> {
                val dto = result.value
                Result.Success(
                    FetchResult(
                        transactions = dto.transactions.map { it.toDomain() },
                        totalCount = dto.totalCount
                    )
                )
            }
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<String> =
        apiService.deleteTransaction(transactionId)
}
