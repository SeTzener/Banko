package com.banko.app.api.auth

import com.banko.app.api.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Loading : AuthState
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
}

class SessionManager(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (authRepository.isLoggedIn) {
            _authState.value = AuthState.Authenticated
            scope.launch {
                when (authRepository.refreshToken()) {
                    is Result.Success -> _authState.value = AuthState.Authenticated
                    is Result.Error -> {
                        authRepository.logout()
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        _authState.value = AuthState.Loading
        return when (val result = authRepository.login(email, password)) {
            is Result.Success -> {
                _authState.value = AuthState.Authenticated
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
                Result.Success(Unit)
            }
            is Result.Error -> {
                _authState.value = AuthState.Unauthenticated
                result
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Unauthenticated
    }
}
