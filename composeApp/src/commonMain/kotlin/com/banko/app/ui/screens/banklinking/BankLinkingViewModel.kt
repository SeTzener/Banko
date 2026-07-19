package com.banko.app.ui.screens.banklinking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banko.app.api.dto.bankoApi.BankAuthorizationStatus
import com.banko.app.api.dto.bankoApi.GetBankAuthorizationsResponse
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BankLinkingViewModel(
    private val apiService: BankoApiService,
) : ViewModel() {

    private val _screenState = MutableStateFlow(BankLinkingScreenState())
    val screenState: StateFlow<BankLinkingScreenState> = _screenState

    private val _settingsBankState = MutableStateFlow(SettingsBankLinkingState())
    val settingsBankState: StateFlow<SettingsBankLinkingState> = _settingsBankState

    init {
        loadBankAuthorizations()
    }

    fun selectCountry(country: Country) {
        _screenState.update {
            it.copy(
                selectedCountry = country,
                currentStep = BankLinkingStep.BankSelection,
                isLoading = true,
                error = null,
                institutions = emptyList(),
            )
        }
        fetchInstitutions(country.code)
    }

    fun selectInstitution(institution: com.banko.app.api.dto.bankoApi.GoCardlessInstitutionDto) {
        _screenState.update {
            it.copy(
                selectedInstitution = institution,
                isLoading = true,
                error = null,
            )
        }
        createEndUserAgreement(institution.id)
    }

    fun onWebViewRedirect(url: String) {
        val currentState = _screenState.value
        val reqId = currentState.requisitionId ?: return

        _screenState.update {
            it.copy(
                currentStep = BankLinkingStep.Processing,
                isLoading = true,
                error = null,
            )
        }
        callBankAuthCallback(reqId)
    }

    fun onWebViewBack() {
        _screenState.update {
            it.copy(
                currentStep = BankLinkingStep.BankSelection,
                authUrl = null,
                selectedInstitution = null,
                error = null,
            )
        }
    }

    fun onWebViewError() {
        _screenState.update {
            it.copy(
                currentStep = BankLinkingStep.Error,
                error = "bank_auth_error",
                isLoading = false,
            )
        }
    }

    fun retry() {
        val state = _screenState.value
        when (state.currentStep) {
            BankLinkingStep.Error -> {
                if (state.selectedInstitution != null) {
                    _screenState.update {
                        it.copy(
                            currentStep = BankLinkingStep.BankSelection,
                            isLoading = false,
                            error = null,
                            authUrl = null,
                        )
                    }
                } else {
                    _screenState.update {
                        it.copy(
                            currentStep = BankLinkingStep.CountrySelection,
                            isLoading = false,
                            error = null,
                            selectedCountry = null,
                        )
                    }
                }
            }
            else -> {}
        }
    }

    fun done() {
        _screenState.update {
            BankLinkingScreenState()
        }
        loadBankAuthorizations()
    }

    fun goBack() {
        val state = _screenState.value
        when (state.currentStep) {
            BankLinkingStep.BankSelection -> {
                _screenState.update {
                    it.copy(
                        currentStep = BankLinkingStep.CountrySelection,
                        selectedCountry = null,
                        institutions = emptyList(),
                        error = null,
                    )
                }
            }
            BankLinkingStep.CountrySelection -> {}
            else -> {}
        }
    }

    fun clearError() {
        _screenState.update { it.copy(error = null) }
    }

    fun reAuthorize(institutionId: String) {
        _screenState.update {
            it.copy(
                currentStep = BankLinkingStep.Authorizing,
                isLoading = true,
                error = null,
            )
        }
        createEndUserAgreement(institutionId)
    }

    private fun fetchInstitutions(countryCode: String) {
        viewModelScope.launch {
            when (val result = apiService.getInstitutions(countryCode)) {
                is Result.Success -> {
                    _screenState.update {
                        it.copy(
                            institutions = result.value,
                            isLoading = false,
                        )
                    }
                }
                is Result.Error -> {
                    _screenState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load banks. Please try again.",
                            currentStep = BankLinkingStep.Error,
                        )
                    }
                }
            }
        }
    }

    private fun createEndUserAgreement(institutionId: String) {
        viewModelScope.launch {
            when (val result = apiService.upsertEndUserAgreement(institutionId)) {
                is Result.Success -> {
                    val response = result.value
                    _screenState.update {
                        it.copy(
                            authUrl = response.link,
                            requisitionId = response.requisitionId,
                            bankAuthorizationId = response.bankAuthorizationId,
                            currentStep = BankLinkingStep.Authorizing,
                            isLoading = false,
                        )
                    }
                }
                is Result.Error -> {
                    _screenState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to start bank authorization. Please try again.",
                            currentStep = BankLinkingStep.Error,
                        )
                    }
                }
            }
        }
    }

    private fun callBankAuthCallback(requisitionId: String) {
        viewModelScope.launch {
            when (val result = apiService.bankAuthCallback(requisitionId)) {
                is Result.Success -> {
                    val response = result.value
                    if (response.status == BankAuthorizationStatus.Linked) {
                        _screenState.update {
                            it.copy(
                                currentStep = BankLinkingStep.Success,
                                linkedAccounts = response.linkedAccounts,
                                isLoading = false,
                                error = null,
                            )
                        }
                    } else {
                        _screenState.update {
                            it.copy(
                                currentStep = BankLinkingStep.Error,
                                error = "Account linking was not completed. Status: ${response.status}",
                                isLoading = false,
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _screenState.update {
                        it.copy(
                            currentStep = BankLinkingStep.Error,
                            error = "Failed to complete bank authorization. Please try again.",
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    private fun loadBankAuthorizations() {
        viewModelScope.launch {
            _settingsBankState.update { it.copy(isLoading = true) }
            when (val result = apiService.getBankAuthorizations()) {
                is Result.Success -> {
                    _settingsBankState.update {
                        it.copy(
                            bankAuthorizations = result.value.bankAuthorizations,
                            isLoading = false,
                        )
                    }
                }
                is Result.Error -> {
                    _settingsBankState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load bank authorizations.",
                        )
                    }
                }
            }
        }
    }
}
