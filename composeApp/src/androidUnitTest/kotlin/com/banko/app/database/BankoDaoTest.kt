package com.banko.app.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.database.Entities.CreditorAccount
import com.banko.app.database.Entities.DebtorAccount
import com.banko.app.database.Entities.ExpenseTag
import com.banko.app.database.Entities.FullTransaction
import com.banko.app.database.Entities.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BankoDaoTest {

    private lateinit var db: BankoDatabase
    private lateinit var dao: BankoDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.bankoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should upsert and query transaction by id`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Payment",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx)

        val loaded = dao.getRawTransactionById("tx-1")
        assertNotNull(loaded)
        assertEquals(tx, loaded)
    }

    @Test
    fun `should return transaction count after upsert`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Payment",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        assertEquals(0, dao.getTransactionCount())
        dao.upsertTransaction(tx)
        assertEquals(1, dao.getTransactionCount())
    }

    @Test
    fun `should update note when saveNote is called`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Payment",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx)
        dao.saveNote("tx-1", "My note")

        val loaded = dao.getRawTransactionById("tx-1")
        assertEquals("My note", loaded?.note)
    }

    @Test
    fun `should remove transaction when deleted`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Payment",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx)
        dao.deleteTransaction("tx-1")
        assertNull(dao.getRawTransactionById("tx-1"))
    }

    @Test
    fun `should return oldest booking date when multiple transactions exist`() = runBlocking {
        val tx1 = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Payment",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )
        val tx2 = Transaction(
            id = "tx-2",
            bookingDate = "2024-02-20T10:30:00",
            valueDate = "2024-02-20T12:00:00",
            amount = "10.00",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Refund",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-2",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx2)
        dao.upsertTransaction(tx1)

        assertEquals("2024-01-15T10:30:00", dao.getOldestTransactions())
    }

    @Test
    fun `should return null when no transactions exist`() = runBlocking {
        assertNull(dao.getOldestTransactions())
    }

    @Test
    fun `should upsert and query expense tag by id`() = runBlocking {
        val tag = ExpenseTag(
            id = "tag-1",
            name = "Groceries",
            color = 0xFF00FF,
            isEarning = false,
            aka = listOf("food", "market")
        )

        dao.upsertExpenseTag(tag)

        val loaded = dao.getExpenseTagById("tag-1").first()
        assertNotNull(loaded)
        assertEquals(tag, loaded)
    }

    @Test
    fun `should return all expense tags`() = runBlocking {
        val tag1 = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        val tag2 = ExpenseTag(id = "tag-2", name = "Salary", color = 0x00FF00, isEarning = true, aka = null)

        dao.upsertExpenseTag(tag1)
        dao.upsertExpenseTag(tag2)

        val tags = dao.getAllExpenseTags().first()
        assertEquals(2, tags.size)
    }

    @Test
    fun `should remove expense tag when deleted`() = runBlocking {
        val tag = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)

        dao.upsertExpenseTag(tag)
        dao.deleteExpenseTag(tag)

        val loaded = dao.getExpenseTagById("tag-1").first()
        assertNull(loaded)
    }

    @Test
    fun `should upsert and query creditor account by id`() = runBlocking {
        val account = CreditorAccount(id = "cred-1", iban = "DE123", bban = "456")

        dao.upsertCreditorAccount(account)

        val loaded = dao.getCreditorAccountById("cred-1").first()
        assertNotNull(loaded)
        assertEquals(account, loaded)
    }

    @Test
    fun `should upsert and query debtor account by id`() = runBlocking {
        val account = DebtorAccount(id = "debt-1", iban = "DE789", bban = "012")

        dao.upsertDebtorAccount(account)

        val loaded = dao.getDebtorAccountById("debt-1").first()
        assertNotNull(loaded)
        assertEquals(account, loaded)
    }

    @Test
    fun `should return transactions ordered by booking date descending`() = runBlocking {
        val tx1 = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Older",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )
        val tx2 = Transaction(
            id = "tx-2",
            bookingDate = "2024-02-20T10:30:00",
            valueDate = "2024-02-20T12:00:00",
            amount = "10.00",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Newer",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-2",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx1)
        dao.upsertTransaction(tx2)

        val result = dao.getTransactionsPagingSource(10).first()
        assertEquals(2, result.size)
        assertEquals("tx-2", result[0].transaction.id)
        assertEquals("tx-1", result[1].transaction.id)
    }

    @Test
    fun `should return only transactions within the given month range`() = runBlocking {
        val tx1 = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "January",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )
        val tx2 = Transaction(
            id = "tx-2",
            bookingDate = "2024-02-20T10:30:00",
            valueDate = "2024-02-20T12:00:00",
            amount = "10.00",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "February",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-2",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx1)
        dao.upsertTransaction(tx2)

        val result = dao.getTransactionsForMonth("2024-01-01", "2024-02-01").first()
        assertEquals(1, result.size)
        assertEquals("tx-1", result[0].transaction.id)
    }

    @Test
    fun `should return full transaction with joined entities`() = runBlocking {
        val creditor = CreditorAccount(id = "cred-1", iban = "DE123", bban = "456")
        val debtor = DebtorAccount(id = "debt-1", iban = "DE789", bban = "012")
        val tag = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        val tx = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = "debt-1",
            remittanceInformationUnstructured = "Joined",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = "Creditor Inc.",
            creditorAccountId = "cred-1",
            debtorName = "John Doe",
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = "tag-1"
        )

        dao.upsertCreditorAccount(creditor)
        dao.upsertDebtorAccount(debtor)
        dao.upsertExpenseTag(tag)
        dao.upsertTransaction(tx)

        val result = dao.getTransactionsPagingSource(10).first()
        assertEquals(1, result.size)
        val full = result[0]
        assertNotNull(full.creditorAccount)
        assertNotNull(full.debtorAccount)
        assertNotNull(full.expenseTag)
        assertEquals("cred-1", full.creditorAccount!!.id)
        assertEquals("debt-1", full.debtorAccount!!.id)
        assertEquals("tag-1", full.expenseTag!!.id)
    }

    @Test
    fun `should return null joins when transaction has no relations`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "No joins",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx)

        val result = dao.getTransactionsPagingSource(10).first()
        assertEquals(1, result.size)
        val full = result[0]
        assertNull(full.creditorAccount)
        assertNull(full.debtorAccount)
        assertNull(full.expenseTag)
    }

    @Test
    fun `should update existing transaction when upserted with same id`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Original",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccountId = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTagId = null
        )

        dao.upsertTransaction(tx)
        val updated = tx.copy(remittanceInformationUnstructured = "Updated")
        dao.upsertTransaction(updated)

        val loaded = dao.getRawTransactionById("tx-1")
        assertEquals("Updated", loaded?.remittanceInformationUnstructured)
    }
}
