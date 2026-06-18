package com.banko.app.data.mapper

import com.banko.app.api.dto.bankoApi.CreditorAccount as DtoCreditorAccount
import com.banko.app.api.dto.bankoApi.DebtorAccount as DtoDebtorAccount
import com.banko.app.api.dto.bankoApi.ExpenseTag as DtoExpenseTag
import com.banko.app.api.dto.bankoApi.Transaction as DtoTransaction
import com.banko.app.database.Entities.CreditorAccount as DaoCreditorAccount
import com.banko.app.database.Entities.DebtorAccount as DaoDebtorAccount
import com.banko.app.database.Entities.ExpenseTag as DaoExpenseTag
import com.banko.app.database.Entities.FullTransaction
import com.banko.app.database.Entities.Transaction as DaoTransaction
import com.banko.app.domain.model.CreditorAccount as DomainCreditorAccount
import com.banko.app.domain.model.DebtorAccount as DomainDebtorAccount
import com.banko.app.domain.model.ExpenseTag as DomainExpenseTag
import com.banko.app.domain.model.Transaction as DomainTransaction
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TransactionMappersTest {

    private val domainTransaction = DomainTransaction(
        id = "tx-1",
        bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
        valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
        amount = 42.50,
        currency = "EUR",
        debtorAccount = DomainDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456"),
        remittanceInformationUnstructured = "Payment",
        remittanceInformationUnstructuredArray = listOf("Payment"),
        bankTransactionCode = "PMNT",
        internalTransactionId = "int-1",
        creditorName = "Creditor Inc.",
        creditorAccount = DomainCreditorAccount(id = "cred-1", iban = "DE789", bban = "012"),
        debtorName = "John Doe",
        remittanceInformationStructuredArray = listOf("INV-001"),
        note = "Test note",
        expenseTag = DomainExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food"))
    )

    private val daoCreditorAccount = DaoCreditorAccount(id = "cred-1", iban = "DE789", bban = "012")
    private val daoDebtorAccount = DaoDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456")
    private val daoExpenseTag = DaoExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food"))

    private val daoTransaction = domainTransaction.toDao()

    private val dtoTransaction = DtoTransaction(
        id = "tx-1",
        bookingDate = "2024-01-15T10:30:00",
        valueDate = "2024-01-15T12:00:00",
        amount = "42.50",
        currency = "EUR",
        debtorAccount = DtoDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456"),
        remittanceInformationUnstructured = "Payment",
        remittanceInformationUnstructuredArray = listOf("Payment"),
        bankTransactionCode = "PMNT",
        internalTransactionId = "int-1",
        creditorName = "Creditor Inc.",
        creditorAccount = DtoCreditorAccount(id = "cred-1", iban = "DE789", bban = "012"),
        debtorName = "John Doe",
        remittanceInformationStructuredArray = listOf("INV-001"),
        note = "Test note",
        expenseTag = DtoExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food"))
    )

    @Test
    fun `DaoCreditorAccount toDomain should map all fields`() {
        val result = daoCreditorAccount.toDomain()
        assertEquals(DomainCreditorAccount(id = "cred-1", iban = "DE789", bban = "012"), result)
    }

    @Test
    fun `DaoDebtorAccount toDomain should map all fields`() {
        val result = daoDebtorAccount.toDomain()
        assertEquals(DomainDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456"), result)
    }

    @Test
    fun `DaoExpenseTag toDomain should map all fields`() {
        val result = daoExpenseTag.toDomain()
        assertEquals(
            DomainExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food")),
            result
        )
    }

    @Test
    fun `DaoExpenseTag toDomain should convert null aka to empty list`() {
        val tag = daoExpenseTag.copy(aka = null)
        assertEquals(emptyList(), tag.toDomain().aka)
    }

    @Test
    fun `DtoCreditorAccount toDomain should map all fields`() {
        val result = DtoCreditorAccount(id = "cred-1", iban = "DE789", bban = "012").toDomain()
        assertEquals(DomainCreditorAccount(id = "cred-1", iban = "DE789", bban = "012"), result)
    }

    @Test
    fun `DtoDebtorAccount toDomain should map all fields`() {
        val result = DtoDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456").toDomain()
        assertEquals(DomainDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456"), result)
    }

    @Test
    fun `DtoExpenseTag toDomain should map all fields`() {
        val result = DtoExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food")).toDomain()
        assertEquals(
            DomainExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food")),
            result
        )
    }

    @Test
    fun `DtoExpenseTag toDomain should convert null aka to empty list`() {
        val tag = DtoExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        assertEquals(emptyList(), tag.toDomain().aka)
    }

    @Test
    fun `DomainCreditorAccount toDao should map all fields`() {
        val result = DomainCreditorAccount(id = "cred-1", iban = "DE789", bban = "012").toDao()
        assertEquals(DaoCreditorAccount(id = "cred-1", iban = "DE789", bban = "012"), result)
    }

    @Test
    fun `DomainDebtorAccount toDao should map all fields`() {
        val result = DomainDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456").toDao()
        assertEquals(DaoDebtorAccount(id = "debtor-1", iban = "DE123", bban = "456"), result)
    }

    @Test
    fun `DomainExpenseTag toDao should map all fields`() {
        val result = DomainExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food")).toDao()
        assertEquals(
            DaoExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = listOf("food")),
            result
        )
    }

    @Test
    fun `DomainExpenseTag toDao should convert empty aka to null`() {
        val result = DomainExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = emptyList()).toDao()
        assertNull(result.aka)
    }

    @Test
    fun `DomainTransaction toDao should map all fields`() {
        val result = domainTransaction.toDao()
        assertEquals(daoTransaction, result)
    }

    @Test
    fun `DomainTransaction toDao should convert null bankTransactionCode to empty string`() {
        val tx = domainTransaction.copy(bankTransactionCode = null)
        val result = tx.toDao()
        assertEquals("", result.bankTransactionCode)
    }

    @Test
    fun `DomainTransaction toDao should map null nested accounts`() {
        val tx = domainTransaction.copy(
            debtorAccount = null,
            creditorAccount = null,
            expenseTag = null
        )
        val result = tx.toDao()
        assertNull(result.debtorAccountId)
        assertNull(result.creditorAccountId)
        assertNull(result.expenseTagId)
    }

    @Test
    fun `FullTransaction toDomain should map all fields`() {
        val fullTx = FullTransaction(
            transaction = daoTransaction,
            creditorAccount = daoCreditorAccount,
            debtorAccount = daoDebtorAccount,
            expenseTag = daoExpenseTag
        )
        val result = fullTx.toDomain()
        assertEquals(domainTransaction, result)
    }

    @Test
    fun `FullTransaction toDomain should map null nested entities`() {
        val fullTx = FullTransaction(
            transaction = daoTransaction.copy(debtorAccountId = null, creditorAccountId = null, expenseTagId = null),
            creditorAccount = null,
            debtorAccount = null,
            expenseTag = null
        )
        val result = fullTx.toDomain()
        assertNull(result.debtorAccount)
        assertNull(result.creditorAccount)
        assertNull(result.expenseTag)
    }

    @Test
    fun `DtoTransaction toDomain should map all fields`() {
        val result = dtoTransaction.toDomain()
        assertEquals(domainTransaction, result)
    }

    @Test
    fun `DtoTransaction toDomain should map null nested entities and nullable fields`() {
        val dto = dtoTransaction.copy(
            debtorAccount = null,
            creditorAccount = null,
            expenseTag = null,
            bankTransactionCode = null,
            creditorName = null,
            debtorName = null,
            note = null,
            remittanceInformationStructuredArray = null
        )
        val result = dto.toDomain()
        val expected = domainTransaction.copy(
            debtorAccount = null,
            creditorAccount = null,
            expenseTag = null,
            bankTransactionCode = null,
            creditorName = null,
            debtorName = null,
            note = null,
            remittanceInformationStructuredArray = null
        )
        assertEquals(expected, result)
    }

    @Test
    fun `List of FullTransaction toDomain should map all items`() {
        val fullTx = FullTransaction(
            transaction = daoTransaction,
            creditorAccount = daoCreditorAccount,
            debtorAccount = daoDebtorAccount,
            expenseTag = daoExpenseTag
        )
        val result = listOf(fullTx).toDomain()
        assertEquals(listOf(domainTransaction), result)
    }

    @Test
    fun `List of FullTransaction toDomain should return empty list for empty input`() {
        val result = emptyList<FullTransaction>().toDomain()
        assertEquals(emptyList(), result)
    }
}
