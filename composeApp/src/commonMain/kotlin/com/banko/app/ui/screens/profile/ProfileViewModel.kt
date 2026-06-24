package com.banko.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.banko.app.api.auth.SessionManager
import com.banko.app.api.auth.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileScreenState(
    val accountId: String? = null,
)

class ProfileViewModel(
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ProfileScreenState(accountId = tokenStorage.accountId)
    )
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    fun logout() {
        sessionManager.logout()
    }
}
