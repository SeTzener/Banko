package com.banko.app.api.dto.bankoApi

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


typealias ModelTransaction = com.banko.app.ui.models.Transaction
typealias ModelDebtorAccount = com.banko.app.ui.models.DebtorAccount
typealias ModelCreditorAccount = com.banko.app.ui.models.CreditorAccount
typealias ModelExpenseTag = com.banko.app.ui.models.ExpenseTag

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
    val bankTransactionCode: String,
    val internalTransactionId: String,
    val creditorName: String? = null,
    val creditorAccount: CreditorAccount? = null,
    val debtorName: String? = null,
    val remittanceInformationStructuredArray: List<String>? = null,
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

fun Transaction.toModelItem() = ModelTransaction(
    id = id,
    bookingDate = LocalDateTime.parse(bookingDate),
    valueDate = LocalDateTime.parse(valueDate),
    amount = amount.toDouble(),
    currency = currency,
    debtorAccount = debtorAccount?.toModelItem(),
    remittanceInformationUnstructured = remittanceInformationUnstructured,
    remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray,
    bankTransactionCode = bankTransactionCode,
    internalTransactionId = internalTransactionId,
    creditorName = creditorName,
    creditorAccount = creditorAccount?.toModelItem(),
    debtorName = debtorName,
    remittanceInformationStructuredArray = remittanceInformationStructuredArray,
    expenseTag = expenseTag?.toModelItem()
)

fun DebtorAccount.toModelItem() = ModelDebtorAccount(
    id = id,
    iban = iban,
    bban = bban
)

fun CreditorAccount.toModelItem() = ModelCreditorAccount(
    id = id,
    iban = iban,
    bban = bban
)

fun ExpenseTag.toModelItem() = ModelExpenseTag(
    id = id,
    name = name,
    color = Color(color),
    aka = aka ?: emptyList()
)