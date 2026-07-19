package com.banko.app.api.dto.bankoApi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BankAuthorizationStatus {
    Processing,
    Linked,
    Error,
    Expired
}

@Serializable
data class GoCardlessInstitutionDto(
    val id: String = "",
    val name: String = "",
    val bic: String = "",
    @SerialName("transaction_total_days")
    val transactionTotalDays: String = "",
    val countries: List<String> = emptyList(),
    val logo: String = "",
    @SerialName("max_access_valid_for_days")
    val maxAccessValidForDays: String = "",
)

@Serializable
data class UpsertEndUserAgreementRequest(
    @SerialName("institutionId")
    val institutionId: String,
    @SerialName("daysOfAccess")
    val daysOfAccess: Int = 90,
)

@Serializable
data class UpsertEndUserAgreementResponse(
    val link: String = "",
    @SerialName("requisitionId")
    val requisitionId: String = "",
    @SerialName("agreementId")
    val agreementId: String = "",
    @SerialName("referenceId")
    val referenceId: String = "",
    @SerialName("institutionId")
    val institutionId: String = "",
    @SerialName("bankAuthorizationId")
    val bankAuthorizationId: String = "",
)

@Serializable
data class BankAuthCallbackRequest(
    @SerialName("requisitionId")
    val requisitionId: String,
)

@Serializable
data class BankAuthCallbackResponse(
    @SerialName("bankAuthorizationId")
    val bankAuthorizationId: String = "",
    val status: BankAuthorizationStatus = BankAuthorizationStatus.Processing,
    @SerialName("linkedAccounts")
    val linkedAccounts: List<LinkedBankAccount> = emptyList(),
)

@Serializable
data class LinkedBankAccount(
    @SerialName("bankAccountId")
    val bankAccountId: String = "",
    val iban: String? = null,
    val currency: String? = null,
    @SerialName("ownerName")
    val ownerName: String? = null,
    @SerialName("accountName")
    val accountName: String? = null,
    val product: String? = null,
)

@Serializable
data class GetBankAuthorizationsResponse(
    @SerialName("bankAuthorizations")
    val bankAuthorizations: List<BankAuthDto> = emptyList(),
)

@Serializable
data class BankAuthDto(
    val id: String = "",
    @SerialName("userId")
    val userId: String = "",
    @SerialName("requisitionId")
    val requisitionId: String? = null,
    @SerialName("institutionId")
    val institutionId: String? = null,
    @SerialName("referenceId")
    val referenceId: String? = null,
    @SerialName("agreementId")
    val agreementId: String? = null,
    val status: BankAuthorizationStatus = BankAuthorizationStatus.Processing,
    @SerialName("institutionName")
    val institutionName: String? = null,
    @SerialName("institutionLogoUrl")
    val institutionLogoUrl: String? = null,
    @SerialName("createdAt")
    val createdAt: String = "",
    @SerialName("updatedAt")
    val updatedAt: String = "",
    val accounts: List<BankAccountSummaryDto> = emptyList(),
)

@Serializable
data class BankAccountSummaryDto(
    @SerialName("bankAccountId")
    val bankAccountId: String = "",
    val iban: String? = null,
    val currency: String? = null,
    @SerialName("ownerName")
    val ownerName: String? = null,
    @SerialName("accountName")
    val accountName: String? = null,
    val product: String? = null,
)
