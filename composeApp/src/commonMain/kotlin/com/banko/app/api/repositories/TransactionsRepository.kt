package com.banko.app.api.repositories

import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result

class TransactionsRepository(
    private val apiService: BankoApiService
) {
    suspend fun getTransactions(): Transactions {
        val result = apiService.getTransactions()
        if (result is Result.Error) {
            println("Error: ${result.error}")
            return Transactions(emptyList(), 0, 0, 0)
        }
        return (result as Result.Success).data
    }
}