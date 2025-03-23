package com.banko.app.database.Entities

import androidx.room.Embedded
import androidx.room.Relation
import com.banko.app.ModelTransaction
import kotlinx.datetime.LocalDateTime

data class FullTransaction(
    @Embedded val transaction: Transaction,

    @Embedded(prefix = "creditor_") val creditorAccount: CreditorAccount? = null,
    @Embedded(prefix = "debtor_") val debtorAccount: DebtorAccount? = null,
    @Embedded(prefix = "expense_") val expenseTag: ExpenseTag? = null
)

fun List<FullTransaction>.toModelItem() = map { it.toModelItem() }

fun FullTransaction.toModelItem() = ModelTransaction(
    id = transaction.id,
    bookingDate = LocalDateTime.parse(transaction.bookingDate),
    valueDate = LocalDateTime.parse(transaction.valueDate),
    amount = transaction.amount.toDouble(),
    currency = transaction.currency,
    debtorAccount = debtorAccount?.toModelItem(),
    remittanceInformationUnstructured = transaction.remittanceInformationUnstructured,
    remittanceInformationUnstructuredArray = transaction.remittanceInformationUnstructuredArray,
    bankTransactionCode = transaction.bankTransactionCode,
    internalTransactionId = transaction.internalTransactionId,
    creditorName = transaction.creditorName,
    creditorAccount = creditorAccount?.toModelItem(),
    debtorName = transaction.debtorName,
    remittanceInformationStructuredArray = transaction.remittanceInformationStructuredArray,
    expenseTag = expenseTag?.toModelItem()
)