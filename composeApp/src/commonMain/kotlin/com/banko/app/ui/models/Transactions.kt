package com.banko.app.ui.models

import com.banko.app.DaoCreditorAccount
import com.banko.app.DaoDebtorAccount
import com.banko.app.DaoTransaction
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
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
    val remittanceInformationStructuredArray: List<String>? = null,
    val note: String? = null,
    val expenseTag: ExpenseTag? = null
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

fun Transaction.toDao() = DaoTransaction(
    id = id,
    bookingDate = bookingDate.toString(),
    valueDate = valueDate.toString(),
    amount = amount.toString(),
    currency = currency,
    debtorAccountId = debtorAccount?.id,
    remittanceInformationUnstructured = remittanceInformationUnstructured,
    remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray,
    bankTransactionCode = bankTransactionCode,
    internalTransactionId = internalTransactionId,
    creditorName = creditorName,
    creditorAccountId = creditorAccount?.id,
    debtorName = debtorName,
    remittanceInformationStructuredArray = remittanceInformationStructuredArray,
    note = note,
    expenseTagId = expenseTag?.id
)

fun DebtorAccount.toDao() = DaoDebtorAccount(
    id = id,
    iban = iban,
    bban = bban
)

fun CreditorAccount.toDao() = DaoCreditorAccount(
    id = id,
    iban = iban,
    bban = bban
)