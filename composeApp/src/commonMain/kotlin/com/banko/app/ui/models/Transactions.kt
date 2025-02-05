package com.banko.app.ui.models

import kotlinx.datetime.LocalDateTime

data class Transaction (
    val id: String,
    val bookingDate: LocalDateTime,
    val valueDate: LocalDateTime,
    val amount: Double,
    val currency: String,
    val debtorAccount: DebtorAccount? = null,
    val remittanceInformationUnstructured: String,
    val remittanceInformationUnstructuredArray: List<String>,
    val bankTransactionCode: String,
    val internalTransactionId: String,
    val creditorName: String? = null,
    val creditorAccount: CreditorAccount? = null,
    val debtorName: String? = null,
    val remittanceInformationStructuredArray: List<String>? = null
)

data class CreditorAccount(
    val id: String,
    val iban: String,
    val bban: String
)

data class DebtorAccount(
    val id: String,
    val iban: String,
    val bban: String
)