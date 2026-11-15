package com.banko.app.api.auth

import com.banko.app.api.utils.Result
import com.banko.app.push.PushRegistrationManager
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock

private const val PROACTIVE_REFRESH_FRACTION = 0.7
private const val MINIMUM_REFRESH_DELAY_MILLIS = 60_000L
private const val PUSH_UNREGISTER_TIMEOUT_MILLIS = 3_000L

sealed interface AuthState {
    data object Loading : AuthState
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
}

class SessionManager(
    private val authRepository: AuthRepository,
    private val pushRegistrationManager: PushRegistrationManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var refreshJob: Job? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        authRepository.onSessionExpired = { handleSessionExpired() }
        if (authRepository.isLoggedIn) {
            _authState.value = AuthState.Authenticated
            pushRegistrationManager.onAuthenticated()
            refreshTokenAndSchedule()
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        _authState.value = AuthState.Loading
        return when (val result = authRepository.login(email, password)) {
            is Result.Success -> {
                _authState.value = AuthState.Authenticated
                pushRegistrationManager.onAuthenticated()
                scheduleProactiveRefresh()
                Result.Success(Unit)
            }
            is Result.Error -> {
                _authState.value = AuthState.Unauthenticated
                result
            }
        }
    }

    suspend fun register(email: String, password: String, fullName: String?, consentGiven: Boolean): Result<Unit> {
        _authState.value = AuthState.Loading
        return when (val result = authRepository.register(email, password, fullName, consentGiven)) {
            is Result.Success -> {
                _authState.value = AuthState.Authenticated
                pushRegistrationManager.onAuthenticated()
                scheduleProactiveRefresh()
                Result.Success(Unit)
            }
            is Result.Error -> {
                _authState.value = AuthState.Unauthenticated
                result
            }
        }
    }

    suspend fun logout() {
        refreshJob?.cancel()
        // Best effort and bounded: signing out must work offline.
        withTimeoutOrNull(PUSH_UNREGISTER_TIMEOUT_MILLIS) {
            pushRegistrationManager.unregisterCurrentToken()
        }
        authRepository.logout()
        _authState.value = AuthState.Unauthenticated
    }

    private fun handleSessionExpired() {
        refreshJob?.cancel()
        authRepository.logout()
        _authState.value = AuthState.Unauthenticated
    }

    private fun refreshTokenAndSchedule() {
        refreshJob = scope.launch {
            when (val result = authRepository.refreshToken()) {
                is Result.Success -> {
                    if (isActive) _authState.value = AuthState.Authenticated
                    // The token fetched at launch may have been expired, so the
                    // first registration attempt can fail. Retry now that the
                    // access token has been refreshed.
                    pushRegistrationManager.onAuthenticated()
                    scheduleProactiveRefresh()
                }
                is Result.Error.HttpError -> {
                    if (isActive && result.code == HttpStatusCode.Unauthorized.value) {
                        handleSessionExpired()
                    } else if (isActive) {
                        _authState.value = AuthState.Authenticated
                        scheduleProactiveRefresh()
                    }
                }
                is Result.Error -> {
                    if (isActive) {
                        _authState.value = AuthState.Authenticated
                        scheduleProactiveRefresh()
                    }
                }
            }
        }
    }

    private fun scheduleProactiveRefresh() {
        refreshJob?.cancel()
        val expiresAt = authRepository.accessTokenExpiresAt ?: return
        val remaining = expiresAt - Clock.System.now().toEpochMilliseconds()
        if (remaining <= 0) {
            refreshTokenAndSchedule()
            return
        }
        val delayMillis =
            (remaining * PROACTIVE_REFRESH_FRACTION).toLong().coerceAtLeast(MINIMUM_REFRESH_DELAY_MILLIS)
        refreshJob = scope.launch {
            delay(delayMillis)
            refreshTokenAndSchedule()
        }
    }
}
