package com.banko.app.api.services

import com.banko.app.api.HttpClientProvider
import com.banko.app.api.auth.TokenStorage
import com.banko.app.api.dto.bankoApi.AcceptConsentRequest
import com.banko.app.api.dto.bankoApi.AuthResponse
import com.banko.app.api.dto.bankoApi.BankAuthCallbackRequest
import com.banko.app.api.dto.bankoApi.BankAuthCallbackResponse
import com.banko.app.api.dto.bankoApi.ChangePasswordRequest
import com.banko.app.api.dto.bankoApi.ExpenseTag
import com.banko.app.api.dto.bankoApi.ExpenseTags
import com.banko.app.api.dto.bankoApi.GetBankAuthorizationsResponse
import com.banko.app.api.dto.bankoApi.GoCardlessInstitutionDto
import com.banko.app.api.dto.bankoApi.LoginRequest
import com.banko.app.api.dto.bankoApi.RefreshRequest
import com.banko.app.api.dto.bankoApi.RegisterRequest
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.dto.bankoApi.UpdateProfileRequest
import com.banko.app.api.dto.bankoApi.UpsertEndUserAgreementRequest
import com.banko.app.api.dto.bankoApi.UpsertEndUserAgreementResponse
import com.banko.app.api.dto.bankoApi.UpsertExpenseTag
import com.banko.app.api.dto.bankoApi.UserExportData
import com.banko.app.api.dto.bankoApi.UserProfileResponse
import com.banko.app.api.utils.getSafe
import com.banko.app.api.utils.postSafe
import com.banko.app.api.utils.putSafe
import com.banko.app.api.utils.deleteSafe
import com.banko.app.api.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BankoApiService(
    client: HttpClient = HttpClient(HttpClientProvider()),
    private val tokenStorage: TokenStorage? = null
) {
    var onSessionExpired: (() -> Unit)? = null

    private val refreshMutex = Mutex()
    private var refreshDeferred: CompletableDeferred<Result<AuthResponse>>? = null

    private val client = tokenStorage?.let { ts ->
        HttpClient(client.engine) {
            HttpClientProvider()()
            install(Auth) {
                bearer {
                    loadTokens {
                        val access = ts.accessToken ?: return@loadTokens null
                        val refresh = ts.refreshToken ?: ""
                        BearerTokens(access, refresh)
                    }
                    refreshTokens {
                        when (val result = singleFlightRefresh()) {
                            is Result.Success ->
                                BearerTokens(result.value.accessToken, result.value.refreshToken)
                            is Result.Error -> null
                        }
                    }
                }
            }
        }
    } ?: client

    fun clearAuthCache() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }

    private val baseUrl = "https://www.bankoapi.space"

    private suspend fun singleFlightRefresh(): Result<AuthResponse> {
        refreshDeferred?.let { return it.await() }
        return refreshMutex.withLock {
            refreshDeferred?.let { return@withLock it.await() }
            val deferred = CompletableDeferred<Result<AuthResponse>>()
            refreshDeferred = deferred
            try {
                deferred.complete(performRefresh())
            } catch (e: Exception) {
                deferred.complete(Result.Error.UnexpectedError(e))
            } finally {
                refreshDeferred = null
            }
            deferred.await()
        }
    }

    private suspend fun performRefresh(): Result<AuthResponse> {
        val ts = tokenStorage
            ?: return Result.Error.UnexpectedError(IllegalStateException("No token storage"))
        val currentRefreshToken = ts.refreshToken
            ?: return Result.Error.UnexpectedError(IllegalStateException("No refresh token"))
        return try {
            val response = client.post("$baseUrl/Users/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken = currentRefreshToken))
                attributes.put(AuthCircuitBreaker, Unit)
            }
            val authResponse = response.body<AuthResponse>()
            ts.accessToken = authResponse.accessToken
            ts.refreshToken = authResponse.refreshToken
            ts.accessTokenExpiresAt =
                Clock.System.now().toEpochMilliseconds() + authResponse.expiresIn * 1000L
            Result.Success(authResponse)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                onSessionExpired?.invoke()
            }
            Result.Error.HttpError(
                code = e.response.status.value,
                description = e.response.status.description,
                message = "Client error: ${e.message}"
            )
        } catch (e: ServerResponseException) {
            Result.Error.HttpError(
                code = e.response.status.value,
                description = e.response.status.description,
                message = "Server error: ${e.message}"
            )
        } catch (e: UnresolvedAddressException) {
            Result.Error.NetworkError(e)
        } catch (e: HttpRequestTimeoutException) {
            Result.Error.NetworkError(e)
        } catch (e: Exception) {
            Result.Error.UnexpectedError(e)
        }
    }

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

    suspend fun refreshToken(): Result<AuthResponse> {
        return singleFlightRefresh()
    }

    suspend fun getProfile(): Result<UserProfileResponse> {
        return client.getSafe("$baseUrl/Users/me") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileResponse> {
        return client.putSafe("$baseUrl/Users/me") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return client.putSafe("$baseUrl/Users/me/password") {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword))
        }
    }

    suspend fun acceptConsent(policyVersionId: String): Result<Unit> {
        return client.putSafe("$baseUrl/Users/me/consent") {
            contentType(ContentType.Application.Json)
            setBody(AcceptConsentRequest(policyVersionId = policyVersionId))
        }
    }

    suspend fun exportData(): Result<UserExportData> {
        return client.getSafe("$baseUrl/Users/me/export") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return client.deleteSafe("$baseUrl/Users/me") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun getInstitutions(country: String): Result<List<GoCardlessInstitutionDto>> {
        return client.getSafe<List<GoCardlessInstitutionDto>>("$baseUrl/Settings/institutions") {
            contentType(ContentType.Application.Json)
            parameter("country", country)
        }
    }

    suspend fun upsertEndUserAgreement(
        institutionId: String,
        daysOfAccess: Int = 90
    ): Result<UpsertEndUserAgreementResponse> {
        return client.postSafe("$baseUrl/Settings/end-user-agreement") {
            contentType(ContentType.Application.Json)
            setBody(
                UpsertEndUserAgreementRequest(
                    institutionId = institutionId,
                    daysOfAccess = daysOfAccess,
                )
            )
        }
    }

    suspend fun bankAuthCallback(requisitionId: String): Result<BankAuthCallbackResponse> {
        return client.postSafe("$baseUrl/Settings/bank-auth-callback") {
            contentType(ContentType.Application.Json)
            setBody(BankAuthCallbackRequest(requisitionId = requisitionId))
        }
    }

    suspend fun getBankAuthorizations(): Result<GetBankAuthorizationsResponse> {
        return client.getSafe("$baseUrl/Settings/BankAuthorization") {
            contentType(ContentType.Application.Json)
        }
    }
}
