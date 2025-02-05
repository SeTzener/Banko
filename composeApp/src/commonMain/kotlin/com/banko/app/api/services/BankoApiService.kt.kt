package com.banko.app.api.services

import com.banko.app.api.HttpClientProvider
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.utils.NetworkError
import com.banko.app.api.utils.getSafe
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import org.koin.core.component.KoinComponent
import com.banko.app.api.utils.Result

class BankoApiService : KoinComponent {
    private val baseUrl = "https://www.bankoapi.space"
    private val client by lazy {
        HttpClient(
            HttpClientProvider()
        )
    }

    suspend fun getTransactions(): Result<Transactions, NetworkError> {
        return client.getSafe<Transactions>("$baseUrl/transactions/") {
            header("Content-Type", "application/json")
        }
    }
}