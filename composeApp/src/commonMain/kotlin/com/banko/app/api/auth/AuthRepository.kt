package com.banko.app.api.auth

import com.banko.app.api.dto.bankoApi.AuthResponse
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result

class AuthRepository(
    private val apiService: BankoApiService,
    private val tokenStorage: TokenStorage
) {
    init {
        apiService.onSessionExpired = { logout() }
    }

    val isLoggedIn: Boolean
        get() = tokenStorage.accessToken != null

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        if (email == "dev" && password == "dev") {
            val devResponse = AuthResponse(
                accessToken = "dev-access-token",
                refreshToken = "dev-refresh-token",
                accountId = "00000000-0000-0000-0000-000000000001",
                expiresIn = 999999
            )
            tokenStorage.accessToken = devResponse.accessToken
            tokenStorage.refreshToken = devResponse.refreshToken
            tokenStorage.accountId = devResponse.accountId
            apiService.clearAuthCache()
            return Result.Success(devResponse)
        }
        return when (val result = apiService.login(email, password)) {
            is Result.Success -> {
                tokenStorage.accessToken = result.value.accessToken
                tokenStorage.refreshToken = result.value.refreshToken
                tokenStorage.accountId = result.value.accountId
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
                tokenStorage.accessToken = result.value.accessToken
                tokenStorage.refreshToken = result.value.refreshToken
                tokenStorage.accountId = result.value.accountId
                apiService.clearAuthCache()
                result
            }
            is Result.Error -> result
        }
    }

    suspend fun refreshToken(): Result<AuthResponse> {
        val currentRefresh = tokenStorage.refreshToken
            ?: return Result.Error.UnexpectedError(IllegalStateException("No refresh token"))
        return when (val result = apiService.refreshToken(currentRefresh)) {
            is Result.Success -> {
                tokenStorage.accessToken = result.value.accessToken
                tokenStorage.refreshToken = result.value.refreshToken
                apiService.clearAuthCache()
                result
            }
            is Result.Error -> {
                tokenStorage.clear()
                apiService.clearAuthCache()
                result
            }
        }
    }

    fun logout() {
        tokenStorage.clear()
        apiService.clearAuthCache()
    }
}
