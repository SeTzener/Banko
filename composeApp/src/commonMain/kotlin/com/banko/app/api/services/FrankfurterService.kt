package com.banko.app.api.services

import com.banko.app.api.HttpClientProvider
import com.banko.app.api.dto.frankfurter.ExchangeRateResponse
import com.banko.app.api.utils.Result
import com.banko.app.api.utils.getSafe
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

class FrankfurterService(
    private val client: HttpClient = HttpClient {
        HttpClientProvider()()
    }
) {
    private val baseUrl = "https://api.frankfurter.app"

    suspend fun getTimeSeriesRates(
        fromCurrency: String,
        toCurrency: String,
        startDate: String,
        endDate: String,
    ): Result<ExchangeRateResponse> {
        return client.getSafe("$baseUrl/$startDate..$endDate") {
            parameter("from", fromCurrency)
            parameter("to", toCurrency)
        }
    }
}
