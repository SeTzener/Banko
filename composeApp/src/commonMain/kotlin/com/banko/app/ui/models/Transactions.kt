package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color
import com.banko.app.DaoCreditorAccount
import com.banko.app.DaoDebtorAccount
import com.banko.app.DaoTransaction
import com.banko.app.domain.model.Transaction as DomainTransaction
import com.banko.app.domain.model.CreditorAccount as DomainCreditorAccount
import com.banko.app.domain.model.DebtorAccount as DomainDebtorAccount
import com.banko.app.domain.model.ExpenseTag as DomainExpenseTag
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

fun Transaction.toDao() = DaoTransaction(
    id = id,
    bookingDate = bookingDate.toString(),
    valueDate = valueDate.toString(),
    amount = amount.toString(),
    currency = currency,
    debtorAccountId = debtorAccount?.id,
    remittanceInformationUnstructured = remittanceInformationUnstructured,
    remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray,
    bankTransactionCode = bankTransactionCode ?: "",
    internalTransactionId = internalTransactionId,
    creditorName = creditorName,
    creditorAccountId = creditorAccount?.id,
    debtorName = debtorName,
    remittanceInformationStructuredArray = remittanceInformationStructuredArray,
    note = note,
    expenseTagId = expenseTag?.id,
    bankName = bankName,
    bankLogoUrl = bankLogoUrl
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

fun DomainTransaction.toUi() = Transaction(
    id = id,
    bookingDate = bookingDate,
    valueDate = valueDate,
    amount = amount,
    currency = currency,
    debtorAccount = debtorAccount?.toUi(),
    remittanceInformationUnstructured = remittanceInformationUnstructured,
    remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray,
    bankTransactionCode = bankTransactionCode,
    internalTransactionId = internalTransactionId,
    creditorName = creditorName,
    creditorAccount = creditorAccount?.toUi(),
    debtorName = debtorName,
    remittanceInformationStructuredArray = remittanceInformationStructuredArray,
    note = note,
    expenseTag = expenseTag?.toUi(),
    bankName = bankName,
    bankLogoUrl = bankLogoUrl
)

fun DomainCreditorAccount.toUi() = CreditorAccount(id = id, iban = iban, bban = bban)
fun DomainDebtorAccount.toUi() = DebtorAccount(id = id, iban = iban, bban = bban)
fun DomainExpenseTag.toUi() = ExpenseTag(
    id = id,
    name = name,
    color = Color(color),
    isEarning = isEarning,
    aka = aka
)