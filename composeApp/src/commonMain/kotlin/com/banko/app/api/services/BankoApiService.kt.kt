package com.banko.app.api.services

import com.banko.app.api.HttpClientProvider
import com.banko.app.api.auth.TokenStorage
import com.banko.app.api.dto.bankoApi.AuthResponse
import com.banko.app.api.dto.bankoApi.ExpenseTag
import com.banko.app.api.dto.bankoApi.ExpenseTags
import com.banko.app.api.dto.bankoApi.LoginRequest
import com.banko.app.api.dto.bankoApi.RefreshRequest
import com.banko.app.api.dto.bankoApi.RegisterRequest
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.dto.bankoApi.UpsertExpenseTag
import com.banko.app.api.utils.getSafe
import com.banko.app.api.utils.postSafe
import com.banko.app.api.utils.putSafe
import com.banko.app.api.utils.deleteSafe
import com.banko.app.api.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BankoApiService(
    client: HttpClient = HttpClient(HttpClientProvider()),
    private val tokenStorage: TokenStorage? = null
) {
    private val client = tokenStorage?.let { ts ->
        HttpClient {
            HttpClientProvider()()
            install(Auth) {
                bearer {
                    loadTokens {
                        val access = ts.accessToken ?: return@loadTokens null
                        val refresh = ts.refreshToken ?: ""
                        BearerTokens(access, refresh)
                    }
                }
            }
        }
    } ?: client

    private val baseUrl = "https://www.bankoapi.space"

    suspend fun getTransactions(
        pageNumber: Int,
        pageSize: Int,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null
    ): Result<Transactions> {
        return client.getSafe<Transactions>("$baseUrl/transactions/") {
            contentType(ContentType.Application.Json)
            parameter("pageNumber", pageNumber)
            parameter("pageSize", pageSize)
            if (fromDate != null) {
                parameter("fromDate", fromDate.toString())
            }
            if (toDate != null) {
                parameter("toDate", toDate.toString())
            }
        }
    }

    suspend fun getExpenseTags(): Result<ExpenseTags> {
        return client.getSafe<ExpenseTags>("$baseUrl/settings/expense-tags") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun updateExpenseTag(expenseTag: ExpenseTag): Result<UpsertExpenseTag> {
        return client.putSafe("$baseUrl/settings/expense-tag/${expenseTag.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                ExpenseTag(
                    id = expenseTag.id,
                    name = expenseTag.name,
                    color = expenseTag.color,
                    isEarning = expenseTag.isEarning,
                    aka = expenseTag.aka
                )
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createExpenseTag(
        name: String,
        color: Long,
        isEarning: Boolean
    ): Result<UpsertExpenseTag> {
        val tagId = Uuid.random().toString()
        return client.postSafe("$baseUrl/settings/expense-tag") {
            contentType(ContentType.Application.Json)
            setBody(
                ExpenseTag(
                    id = tagId,
                    name = name,
                    color = color,
                    isEarning = isEarning,
                    aka = null
                )
            )
        }
    }

    suspend fun deleteExpenseTag(expenseTagId: String): Result<Unit> {
        return client.deleteSafe("$baseUrl/settings/expense-tag/${expenseTagId}") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun assignExpenseTag(id: String, expenseTagId: String?): Result<Unit> {
        return client.putSafe("$baseUrl/transactions/expense-tag") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "transactionId" to id,
                    "expenseTagId" to expenseTagId
                )
            )
        }
    }

    suspend fun saveNote( id: String, text: String): Result<String> {
        return client.putSafe("$baseUrl/transactions/${id}/note") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "note" to text
                )
            )
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<String> {
        return client.deleteSafe("$baseUrl/transactions/${transactionId}") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return client.postSafe("$baseUrl/Users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = email, password = password))
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String?,
        consentGiven: Boolean
    ): Result<AuthResponse> {
        return client.postSafe("$baseUrl/Users") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    email = email,
                    password = password,
                    fullName = fullName,
                    consentGiven = consentGiven
                )
            )
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<AuthResponse> {
        return client.postSafe("$baseUrl/Users/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = refreshToken))
        }
    }
}