package com.banko.app.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.auth.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterScreenState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
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

    fun onFullNameChanged(value: String) {
        _state.update { it.copy(fullName = value, error = null) }
    }

    fun onConsentChanged(value: Boolean) {
        _state.update { it.copy(consentGiven = value, error = null) }
    }

    fun register() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Please fill in all fields.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        sessionManager.register(
            email = s.email,
            password = s.password,
            fullName = s.fullName.ifBlank { null },
            consentGiven = s.consentGiven,
        )
    }
}
