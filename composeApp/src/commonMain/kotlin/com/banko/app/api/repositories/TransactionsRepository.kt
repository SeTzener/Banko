package com.banko.app.api.repositories

import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result

class TransactionsRepository(
    private val apiService: BankoApiService
) {
    // TODO(): change this to return a model item
    suspend fun getTransactions(pageNumber: Int, pageSize: Int): Transactions {
        val result = apiService.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
        if (result is Result.Error) {
            println("Error: $result")
            return Transactions(emptyList(), 0, 0, 0)
        }
        return (result as Result.Success).value
    }
}