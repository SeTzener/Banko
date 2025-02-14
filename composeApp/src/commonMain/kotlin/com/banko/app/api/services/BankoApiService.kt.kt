package com.banko.app.api.services

import com.banko.app.api.HttpClientProvider
import com.banko.app.api.dto.bankoApi.ExpenseTag
import com.banko.app.api.dto.bankoApi.ExpenseTags
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.dto.bankoApi.UpsertExpenseTag
import com.banko.app.api.utils.NetworkError
import com.banko.app.api.utils.getSafe
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import org.koin.core.component.KoinComponent
import com.banko.app.api.utils.Result
import com.banko.app.api.utils.deleteSafe
import com.banko.app.api.utils.postSafe
import com.banko.app.api.utils.putSafe
import com.banko.config.BuildKonfig
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.header.AcceptEncoding

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

    suspend fun getExpenseTags(): Result<ExpenseTags, NetworkError> {
        return client.getSafe<ExpenseTags>("$baseUrl/settings/expense-tags") {
            header("Content-Type", "application/json")
        }
    }

    suspend fun updateExpenseTag(expenseTag: ExpenseTag): Result<UpsertExpenseTag, NetworkError> {
        return client.putSafe("$baseUrl/settings/expense-tag/${expenseTag.id}") {
            contentType(ContentType.Application.Json)
            AcceptEncoding("application/json")
            setBody(
                mapOf(
                    "id" to expenseTag.id,
                    "name" to expenseTag.name,
                    "color" to expenseTag.color.toString(),
                    "aka" to expenseTag.aka
                )
            )
        }
    }

    suspend fun createExpenseTag(expenseTag: ExpenseTag): Result<UpsertExpenseTag, NetworkError> {
        return client.postSafe("$baseUrl/settings/expense-tag") {
            contentType(ContentType.Application.Json)
            AcceptEncoding("application/json")
            setBody(
                mapOf(
                    "id" to expenseTag.id,
                    "name" to expenseTag.name,
                    "color" to expenseTag.color.toString(),
                    "aka" to expenseTag.aka
                )
            )
        }
    }

    suspend fun deleteExpenseTag(expenseTagId: String): Result<Unit, NetworkError> {
        return client.deleteSafe("$baseUrl/settings/expense-tag/${expenseTagId}") {
            contentType(ContentType.Application.Json)
            AcceptEncoding("application/json")
        }
    }
}