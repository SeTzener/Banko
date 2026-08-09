package com.banko.app.api.auth

import com.banko.app.api.dto.bankoApi.AuthResponse
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock

class AuthRepository(
    private val apiService: BankoApiService,
    private val tokenStorage: TokenStorage
) {
    var onSessionExpired: (() -> Unit)? = null

    init {
        apiService.onSessionExpired = { sessionExpired() }
    }

    val isLoggedIn: Boolean
        get() = tokenStorage.accessToken != null

    val accessTokenExpiresAt: Long?
        get() = tokenStorage.accessTokenExpiresAt

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return when (val result = apiService.login(email, password)) {
            is Result.Success -> {
                persistAuthResponse(result.value)
                apiService.clearAuthCache()
                result
            }
            is Result.Error -> result
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String?,
        consentGiven: Boolean
    ): Result<AuthResponse> {
        return when (val result = apiService.register(email, password, fullName, consentGiven)) {
            is Result.Success -> {
                persistAuthResponse(result.value)
                apiService.clearAuthCache()
                result
            }
            is Result.Error -> result
        }
    }

    suspend fun refreshToken(): Result<AuthResponse> {
        if (tokenStorage.refreshToken == null) {
            return Result.Error.UnexpectedError(IllegalStateException("No refresh token"))
        }
        return when (val result = apiService.refreshToken()) {
            is Result.Success -> {
                apiService.clearAuthCache()
                result
            }
            is Result.Error.HttpError -> {
                if (result.code == HttpStatusCode.Unauthorized.value) {
                    sessionExpired()
                }
                result
            }
            is Result.Error -> result
        }
    }

    fun logout() {
        tokenStorage.clear()
        apiService.clearAuthCache()
    }

    private fun sessionExpired() {
        logout()
        onSessionExpired?.invoke()
    }

    private fun persistAuthResponse(response: AuthResponse) {
        tokenStorage.accessToken = response.accessToken
        tokenStorage.refreshToken = response.refreshToken
        tokenStorage.accountId = response.accountId
        tokenStorage.accessTokenExpiresAt =
            Clock.System.now().toEpochMilliseconds() + response.expiresIn * 1000L
    }
}
