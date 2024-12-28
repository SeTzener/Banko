package com.banko.app.api.dto.nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transactions(
    @SerialName("transactions")
    val bankTransactions: BankTransactions,
)

@Serializable
data class BankTransactions(
    val booked: List<Booked>,
    val pending: List<Pending>,
)

@Serializable
data class Booked(
    val transactionId: String,
    val bookingDate: String,
    val valueDate: String,
    val transactionAmount: TransactionAmount,
    val debtorAccount: DebtorAccount? = null,
    val remittanceInformationUnstructured: String,
    val remittanceInformationUnstructuredArray: List<String>,
    val bankTransactionCode: String,
    val internalTransactionId: String,
    val creditorName: String? = null,
    val creditorAccount: CreditorAccount? = null,
    val debtorName: String? = null,
    val remittanceInformationStructuredArray: List<String>? = null,
)

@Serializable
data class TransactionAmount(
    val amount: String,
    val currency: String,
)

@Serializable
data class DebtorAccount(
    val iban: String,
    val bban: String,
)

@Serializable
data class CreditorAccount(
    val iban: String,
    val bban: String,
)

@Serializable
data class Pending(
    val bookingDate: String,
    val transactionAmount: TransactionAmount2,
    val remittanceInformationUnstructured: String,
    val remittanceInformationUnstructuredArray: List<String>,
)

@Serializable
data class TransactionAmount2(
    val amount: String,
    val currency: String,
)
