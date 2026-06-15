package com.banko.app.data.mapper

import com.banko.app.database.Entities.FullTransaction
import com.banko.app.domain.model.CreditorAccount as DomainCreditorAccount
import com.banko.app.domain.model.DebtorAccount as DomainDebtorAccount
import com.banko.app.domain.model.ExpenseTag as DomainExpenseTag
import com.banko.app.domain.model.Transaction as DomainTransaction
import com.banko.app.database.Entities.Transaction as DaoTransaction
import com.banko.app.database.Entities.CreditorAccount as DaoCreditorAccount
import com.banko.app.database.Entities.DebtorAccount as DaoDebtorAccount
import com.banko.app.database.Entities.ExpenseTag as DaoExpenseTag
import com.banko.app.api.dto.bankoApi.Transaction as DtoTransaction
import com.banko.app.api.dto.bankoApi.CreditorAccount as DtoCreditorAccount
import com.banko.app.api.dto.bankoApi.DebtorAccount as DtoDebtorAccount
import com.banko.app.api.dto.bankoApi.ExpenseTag as DtoExpenseTag
import kotlinx.datetime.LocalDateTime

fun FullTransaction.toDomain(): DomainTransaction = DomainTransaction(
    id = transaction.id,
    bookingDate = LocalDateTime.parse(transaction.bookingDate),
    valueDate = LocalDateTime.parse(transaction.valueDate),
    amount = transaction.amount.toDouble(),
    currency = transaction.currency,
    debtorAccount = debtorAccount?.toDomain(),
    remittanceInformationUnstructured = transaction.remittanceInformationUnstructured,
    remittanceInformationUnstructuredArray = transaction.remittanceInformationUnstructuredArray,
    bankTransactionCode = transaction.bankTransactionCode,
    internalTransactionId = transaction.internalTransactionId,
    creditorName = transaction.creditorName,
    creditorAccount = creditorAccount?.toDomain(),
    debtorName = transaction.debtorName,
    remittanceInformationStructuredArray = transaction.remittanceInformationStructuredArray,
    note = transaction.note,
    expenseTag = expenseTag?.toDomain()
)

fun List<FullTransaction>.toDomain(): List<DomainTransaction> = map { it.toDomain() }

fun DaoCreditorAccount.toDomain() = DomainCreditorAccount(id = id, iban = iban, bban = bban)
fun DaoDebtorAccount.toDomain() = DomainDebtorAccount(id = id, iban = iban, bban = bban)
fun DaoExpenseTag.toDomain() = DomainExpenseTag(
    id = id,
    name = name,
    color = color,
    isEarning = isEarning,
    aka = aka ?: emptyList()
)

fun DtoTransaction.toDomain(): DomainTransaction = DomainTransaction(
    id = id,
    bookingDate = LocalDateTime.parse(bookingDate),
    valueDate = LocalDateTime.parse(valueDate),
    amount = amount.toDouble(),
    currency = currency,
    debtorAccount = debtorAccount?.toDomain(),
    remittanceInformationUnstructured = remittanceInformationUnstructured,
    remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray,
    bankTransactionCode = bankTransactionCode,
    internalTransactionId = internalTransactionId,
    creditorName = creditorName,
    creditorAccount = creditorAccount?.toDomain(),
    debtorName = debtorName,
    remittanceInformationStructuredArray = remittanceInformationStructuredArray,
    note = note,
    expenseTag = expenseTag?.toDomain()
)

fun DtoCreditorAccount.toDomain() = DomainCreditorAccount(id = id, iban = iban, bban = bban)
fun DtoDebtorAccount.toDomain() = DomainDebtorAccount(id = id, iban = iban, bban = bban)
fun DtoExpenseTag.toDomain() = DomainExpenseTag(
    id = id,
    name = name,
    color = color,
    isEarning = isEarning,
    aka = aka ?: emptyList()
)

fun DomainTransaction.toDao(): DaoTransaction = DaoTransaction(
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
    expenseTagId = expenseTag?.id
)

fun DomainCreditorAccount.toDao() = DaoCreditorAccount(id = id, iban = iban, bban = bban)
fun DomainDebtorAccount.toDao() = DaoDebtorAccount(id = id, iban = iban, bban = bban)
fun DomainExpenseTag.toDao() = DaoExpenseTag(
    id = id,
    name = name,
    color = color,
    isEarning = isEarning,
    aka = if (aka.isEmpty()) null else aka
)
