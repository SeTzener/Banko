package com.banko.app.api.dto.bankoApi

import kotlinx.serialization.Serializable

@Serializable
data class Transactions(
    val transactions: List<Transaction>,
    val totalCount: Long,
    val pageNumber: Long,
    val pageSize: Long,
)

@Serializable
data class Transaction(
    val id: String,
    val bookingDate: String,
    val valueDate: String,
    val amount: String,
    val currency: String,
    val debtorAccount: DebtorAccount? = null,
    val remittanceInformationUnstructured: String,
    val remittanceInformationUnstructuredArray: List<String>,
    val bankTransactionCode: String?,
    val internalTransactionId: String,
    val creditorName: String? = null,
    val creditorAccount: CreditorAccount? = null,
    val debtorName: String? = null,
    val remittanceInformationStructuredArray: List<String>? = null,
    val note: String? = null,
    val expenseTag: ExpenseTag? = null,
    val bankName: String? = null,
    val bankLogoUrl: String? = null
)

@Serializable
data class CreditorAccount(
    val id: String,
    val iban: String,
    val bban: String
)

@Serializable
data class DebtorAccount(
    val id: String,
    val iban: String,
    val bban: String
)