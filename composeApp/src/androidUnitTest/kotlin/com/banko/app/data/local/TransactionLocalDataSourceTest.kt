package com.banko.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExpenseTag as DaoExpenseTag
import com.banko.app.domain.model.CreditorAccount
import com.banko.app.domain.model.DebtorAccount
import com.banko.app.domain.model.ExpenseTag
import com.banko.app.domain.model.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionLocalDataSourceTest {

    private lateinit var dataSource: TransactionLocalDataSource
    private lateinit var db: BankoDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataSource = TransactionLocalDataSource(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should upsert and retrieve transaction`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Payment",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )

        dataSource.upsertTransaction(tx)

        val result = dataSource.getTransactions(10).first()
        assertEquals(1, result.size)
        assertEquals(tx, result[0])
    }

    @Test
    fun `should preserve nested creditor, debtor and expense tag relations`() = runBlocking {
        val creditor = CreditorAccount(id = "cred-1", iban = "DE123", bban = "456")
        val debtor = DebtorAccount(id = "debt-1", iban = "DE789", bban = "012")
        val tag = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = emptyList())
        val tx = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = debtor,
            remittanceInformationUnstructured = "Nested",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = "Creditor Inc.",
            creditorAccount = creditor,
            debtorName = "John Doe",
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = tag
        )

        dataSource.upsertTransaction(tx)

        val result = dataSource.getTransactions(10).first()
        assertEquals(1, result.size)
        val loaded = result[0]
        assertNotNull(loaded.creditorAccount)
        assertNotNull(loaded.debtorAccount)
        assertNotNull(loaded.expenseTag)
        assertEquals("cred-1", loaded.creditorAccount!!.id)
        assertEquals("debt-1", loaded.debtorAccount!!.id)
        assertEquals("tag-1", loaded.expenseTag!!.id)
    }

    @Test
    fun `should update existing transaction when upserted twice`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Original",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )

        dataSource.upsertTransaction(tx)
        dataSource.upsertTransaction(tx.copy(remittanceInformationUnstructured = "Updated"))

        val result = dataSource.getTransactions(10).first()
        assertEquals("Updated", result[0].remittanceInformationUnstructured)
    }

    @Test
    fun `should return correct transaction count`() = runBlocking {
        assertEquals(0, dataSource.getStoredTransactionCount())

        val tx1 = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "First",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        val tx2 = Transaction(
            id = "tx-2",
            bookingDate = LocalDateTime.parse("2024-02-20T10:30:00"),
            valueDate = LocalDateTime.parse("2024-02-20T12:00:00"),
            amount = 10.00,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Second",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-2",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )

        dataSource.upsertTransaction(tx1)
        assertEquals(1, dataSource.getStoredTransactionCount())
        dataSource.upsertTransaction(tx2)
        assertEquals(2, dataSource.getStoredTransactionCount())
    }

    @Test
    fun `should return oldest transaction date when multiple exist`() = runBlocking {
        val tx2 = Transaction(
            id = "tx-2",
            bookingDate = LocalDateTime.parse("2024-02-20T10:30:00"),
            valueDate = LocalDateTime.parse("2024-02-20T12:00:00"),
            amount = 10.00,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Later",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-2",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        val tx1 = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Earlier",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )

        dataSource.upsertTransaction(tx2)
        dataSource.upsertTransaction(tx1)

        val expected = LocalDateTime.parse("2024-01-15T10:30:00")
        assertEquals(expected, dataSource.getOldestTransactions())
    }

    @Test
    fun `should return only transactions within the given date range`() = runBlocking {
        val janTx = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "January",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        val febTx = Transaction(
            id = "tx-2",
            bookingDate = LocalDateTime.parse("2024-02-20T10:30:00"),
            valueDate = LocalDateTime.parse("2024-02-20T12:00:00"),
            amount = 10.00,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "February",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-2",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )

        dataSource.upsertTransaction(janTx)
        dataSource.upsertTransaction(febTx)

        val janResults = dataSource.getTransactionsForDateRange(
            LocalDate(2024, 1, 1),
            LocalDate(2024, 1, 31)
        ).first()
        assertEquals(1, janResults.size)
        assertEquals("tx-1", janResults[0].id)
    }

    @Test
    fun `should remove transaction when deleted`() = runBlocking {
        val tx = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "To delete",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )

        dataSource.upsertTransaction(tx)
        assertEquals(1, dataSource.getStoredTransactionCount())

        dataSource.deleteTransaction("tx-1")
        assertEquals(0, dataSource.getStoredTransactionCount())
    }

    @Test
    fun `should respect the limit parameter when fetching transactions`() = runBlocking {
        val tx1 = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "First",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-1",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        val tx2 = Transaction(
            id = "tx-2",
            bookingDate = LocalDateTime.parse("2024-02-20T10:30:00"),
            valueDate = LocalDateTime.parse("2024-02-20T12:00:00"),
            amount = 10.00,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Second",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-2",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )

        dataSource.upsertTransaction(tx1)
        dataSource.upsertTransaction(tx2)

        val limited = dataSource.getTransactions(1).first()
        assertEquals(1, limited.size)
    }
}
