package com.banko.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.api.utils.Result
import com.banko.app.data.local.TransactionLocalDataSource
import com.banko.app.data.remote.TransactionRemoteDataSource
import com.banko.app.domain.model.CreditorAccount
import com.banko.app.domain.model.DebtorAccount
import com.banko.app.domain.model.ExpenseTag
import com.banko.app.domain.model.Transaction
import com.banko.app.database.BankoDatabase
import io.mockk.coEvery
import io.mockk.mockk
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
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionRepositoryTest {

    private lateinit var repo: TransactionRepository
    private lateinit var db: BankoDatabase
    private val remote = mockk<TransactionRemoteDataSource>(relaxed = true)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val local = TransactionLocalDataSource(db)
        repo = TransactionRepository(local, remote)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── fetchAndStoreTransactions coordination ────────────────────

    @Test
    fun `should not store transactions locally when remote fetch fails`() = runBlocking {
        coEvery { remote.fetchTransactions(1, 50) } returns Result.Error.HttpError(500, "Server error")

        val result = repo.fetchAndStoreTransactions(1, 50)

        assertTrue(result is Result.Error)
        val stored = repo.getTransactions(10).first()
        assertTrue(stored.isEmpty())
    }

    // ── fetchAndStoreTransactionsForDateRange coordination ────────

    @Test
    fun `should store date-range transactions locally when remote fetch succeeds`() = runBlocking {
        val tx = Transaction(
            id = "tx-range",
            bookingDate = LocalDateTime.parse("2024-06-10T10:30:00"),
            valueDate = LocalDateTime.parse("2024-06-10T12:00:00"),
            amount = 99.99,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Date range",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-range",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        val fetchResult = TransactionRemoteDataSource.FetchResult(
            transactions = listOf(tx),
            totalCount = 1
        )
        coEvery {
            remote.fetchTransactionsForDateRange(LocalDate(2024, 6, 1), LocalDate(2024, 6, 30))
        } returns Result.Success(fetchResult)

        repo.fetchAndStoreTransactionsForDateRange(LocalDate(2024, 6, 1), LocalDate(2024, 6, 30))

        val stored = repo.getTransactions(10).first()
        assertEquals(1, stored.size)
        assertEquals("tx-range", stored[0].id)
    }

    @Test
    fun `should not store date-range transactions locally when remote fetch fails`() = runBlocking {
        coEvery {
            remote.fetchTransactionsForDateRange(LocalDate(2024, 6, 1), LocalDate(2024, 6, 30))
        } returns Result.Error.HttpError(500, "Server error")

        repo.fetchAndStoreTransactionsForDateRange(LocalDate(2024, 6, 1), LocalDate(2024, 6, 30))

        val stored = repo.getTransactions(10).first()
        assertTrue(stored.isEmpty())
    }

    // ── deleteTransaction coordination ────────────────────────────

    @Test
    fun `should delete transaction locally when remote delete succeeds`() = runBlocking {
        val tx = Transaction(
            id = "tx-del",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 10.00,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "To delete",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-del",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        db.bankoDao().upsertTransaction(
            com.banko.app.database.Entities.Transaction(
                id = tx.id, bookingDate = "2024-01-15T10:30:00", valueDate = "2024-01-15T12:00:00",
                amount = "10.00", currency = "EUR", debtorAccountId = null,
                remittanceInformationUnstructured = "To delete", remittanceInformationUnstructuredArray = emptyList(),
                bankTransactionCode = "PMNT", internalTransactionId = "int-del",
                creditorName = null, creditorAccountId = null, debtorName = null,
                remittanceInformationStructuredArray = null, note = null, expenseTagId = null
            )
        )
        coEvery { remote.deleteTransaction("tx-del") } returns Result.Success("Deleted")

        repo.deleteTransaction("tx-del")

        val stored = repo.getTransactions(10).first()
        assertTrue(stored.isEmpty())
    }

    @Test
    fun `should not delete transaction locally when remote delete fails`() = runBlocking {
        val tx = Transaction(
            id = "tx-keep",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 10.00,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Keep",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-keep",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        db.bankoDao().upsertTransaction(
            com.banko.app.database.Entities.Transaction(
                id = tx.id, bookingDate = "2024-01-15T10:30:00", valueDate = "2024-01-15T12:00:00",
                amount = "10.00", currency = "EUR", debtorAccountId = null,
                remittanceInformationUnstructured = "Keep", remittanceInformationUnstructuredArray = emptyList(),
                bankTransactionCode = "PMNT", internalTransactionId = "int-keep",
                creditorName = null, creditorAccountId = null, debtorName = null,
                remittanceInformationStructuredArray = null, note = null, expenseTagId = null
            )
        )
        coEvery { remote.deleteTransaction("tx-keep") } returns Result.Error.HttpError(500, "Server error")

        repo.deleteTransaction("tx-keep")

        val stored = repo.getTransactions(10).first()
        assertEquals(1, stored.size)
    }

    // ── fetchAndStoreTransactions with nested entities ────────────

    @Test
    fun `should store transactions with nested creditor, debtor and expense tag`() = runBlocking {
        val creditor = CreditorAccount(id = "cred-1", iban = "DE123", bban = "456")
        val debtor = DebtorAccount(id = "debt-1", iban = "DE789", bban = "012")
        val tag = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = emptyList())
        val tx = Transaction(
            id = "tx-nested",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = debtor,
            remittanceInformationUnstructured = "Nested",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-nested",
            creditorName = "Creditor Inc.",
            creditorAccount = creditor,
            debtorName = "John Doe",
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = tag
        )
        val fetchResult = TransactionRemoteDataSource.FetchResult(
            transactions = listOf(tx),
            totalCount = 1
        )
        coEvery { remote.fetchTransactions(1, 10) } returns Result.Success(fetchResult)

        val result = repo.fetchAndStoreTransactions(1, 10)

        assertTrue(result is Result.Success)
        val stored = repo.getTransactions(10).first()
        assertEquals(1, stored.size)
        val loaded = stored[0]
        assertEquals("cred-1", loaded.creditorAccount?.id)
        assertEquals("debt-1", loaded.debtorAccount?.id)
        assertEquals("tag-1", loaded.expenseTag?.id)
    }

    @Test
    fun `should store multiple transactions from remote`() = runBlocking {
        val tx1 = Transaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 10.00,
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
            amount = 20.00,
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
        val fetchResult = TransactionRemoteDataSource.FetchResult(
            transactions = listOf(tx1, tx2),
            totalCount = 2
        )
        coEvery { remote.fetchTransactions(1, 50) } returns Result.Success(fetchResult)

        repo.fetchAndStoreTransactions(1, 50)

        val stored = repo.getTransactions(10).first()
        assertEquals(2, stored.size)
    }

    @Test
    fun `should return correct total count from remote result`() = runBlocking {
        val tx = Transaction(
            id = "tx-cnt",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 10.00,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Count",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-cnt",
            creditorName = null,
            creditorAccount = null,
            debtorName = null,
            remittanceInformationStructuredArray = null,
            note = null,
            expenseTag = null
        )
        val fetchResult = TransactionRemoteDataSource.FetchResult(
            transactions = listOf(tx),
            totalCount = 100
        )
        coEvery { remote.fetchTransactions(1, 10) } returns Result.Success(fetchResult)

        val result = repo.fetchAndStoreTransactions(1, 10)

        assertTrue(result is Result.Success)
        assertEquals(100L, result.value)
    }
}
