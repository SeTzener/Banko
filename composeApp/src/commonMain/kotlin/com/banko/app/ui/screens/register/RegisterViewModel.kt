package com.banko.app.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.auth.SessionManager
import com.banko.app.api.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterScreenState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val surname: String = "",
    val consentGiven: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class RegisterViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterScreenState())
    val state: StateFlow<RegisterScreenState> = _state.asStateFlow()

    fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChanged(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    fun onNameChanged(value: String) {
        _state.update { it.copy(name = value, error = null) }
    }

    fun onSurnameChanged(value: String) {
        _state.update { it.copy(surname = value, error = null) }
    }

    fun onConsentChanged(value: Boolean) {
        _state.update { it.copy(consentGiven = value, error = null) }
    }

    fun register() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank() || s.name.isBlank() || s.surname.isBlank()) {
            _state.update { it.copy(error = "Please fill in all fields.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val fullName = "${s.surname} ${s.name}"
            when (val result = sessionManager.register(
                email = s.email,
                password = s.password,
                fullName = fullName,
                consentGiven = s.consentGiven,
            )) {
                is Result.Success -> _state.update { it.copy(isLoading = false) }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = errorMessageFor(result))
                }
            }
        }
    }

    private fun errorMessageFor(error: Result.Error): String = when (error) {
        is Result.Error.HttpError -> when (error.code) {
            409 -> "An account with this email already exists."
            in 500..599 -> "Server not responding. Check your connection and try again."
            else -> "An unexpected error occurred. Please try again."
        }
        is Result.Error.NetworkError -> "Server not responding. Check your connection and try again."
        else -> "An unexpected error occurred. Please try again."
    }
}
