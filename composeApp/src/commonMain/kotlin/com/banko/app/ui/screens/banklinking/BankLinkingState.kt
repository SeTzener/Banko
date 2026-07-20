package com.banko.app.ui.screens.banklinking

import com.banko.app.api.dto.bankoApi.BankAuthDto
import com.banko.app.api.dto.bankoApi.GoCardlessInstitutionDto
import com.banko.app.api.dto.bankoApi.LinkedBankAccount

enum class BankLinkingStep {
    CountrySelection,
    BankSelection,
    Authorizing,
    Processing,
    Success,
    Error,
}

data class Country(
    val code: String,
    val name: String,
)

val popularCountries = listOf(
    Country("NO", "Norway"),
    Country("SE", "Sweden"),
    Country("DE", "Germany"),
    Country("GB", "United Kingdom"),
    Country("FR", "France"),
    Country("ES", "Spain"),
    Country("NL", "Netherlands"),
    Country("FI", "Finland"),
    Country("DK", "Denmark"),
    Country("AT", "Austria"),
    Country("IT", "Italy"),
    Country("PL", "Poland"),
)

val allCountries = listOf(
    Country("AT", "Austria"),
    Country("BE", "Belgium"),
    Country("BG", "Bulgaria"),
    Country("HR", "Croatia"),
    Country("CY", "Cyprus"),
    Country("CZ", "Czech Republic"),
    Country("DK", "Denmark"),
    Country("EE", "Estonia"),
    Country("FI", "Finland"),
    Country("FR", "France"),
    Country("DE", "Germany"),
    Country("GR", "Greece"),
    Country("HU", "Hungary"),
    Country("IE", "Ireland"),
    Country("IT", "Italy"),
    Country("LV", "Latvia"),
    Country("LT", "Lithuania"),
    Country("LU", "Luxembourg"),
    Country("MT", "Malta"),
    Country("NL", "Netherlands"),
    Country("NO", "Norway"),
    Country("PL", "Poland"),
    Country("PT", "Portugal"),
    Country("RO", "Romania"),
    Country("SK", "Slovakia"),
    Country("SI", "Slovenia"),
    Country("ES", "Spain"),
    Country("SE", "Sweden"),
    Country("GB", "United Kingdom"),
)

data class BankLinkingScreenState(
    val currentStep: BankLinkingStep = BankLinkingStep.CountrySelection,
    val selectedCountry: Country? = null,
    val institutions: List<GoCardlessInstitutionDto> = emptyList(),
    val selectedInstitution: GoCardlessInstitutionDto? = null,
    val authUrl: String? = null,
    val requisitionId: String? = null,
    val bankAuthorizationId: String? = null,
    val linkedAccounts: List<LinkedBankAccount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class SettingsBankLinkingState(
    val bankAuthorizations: List<BankAuthDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
