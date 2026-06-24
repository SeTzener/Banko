package com.banko.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.auth.SessionManager
import com.banko.app.api.auth.TokenStorage
import com.banko.app.api.dto.bankoApi.UserExportData
import com.banko.app.api.dto.bankoApi.UserProfileResponse
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileScreenState(
    val accountId: String? = null,
    val profile: UserProfileResponse? = null,
    val exportData: UserExportData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
)

class ProfileViewModel(
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
    private val apiService: BankoApiService,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ProfileScreenState(accountId = tokenStorage.accountId)
    )
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = apiService.getProfile()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        profile = result.value,
                        isLoading = false,
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load profile"
                    )
                }
            }
        }
    }

    fun acceptConsent(policyVersionId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (apiService.acceptConsent(policyVersionId)) {
                is Result.Success -> {
                    loadProfile()
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to update consent"
                    )
                }
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = apiService.exportData()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        exportData = result.value,
                        isLoading = false,
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to export data"
                    )
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (apiService.deleteAccount()) {
                is Result.Success -> {
                    tokenStorage.clear()
                    sessionManager.logout()
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to delete account"
                    )
                }
            }
        }
    }

    fun clearExportData() {
        _state.value = _state.value.copy(exportData = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun logout() {
        sessionManager.logout()
    }
}
