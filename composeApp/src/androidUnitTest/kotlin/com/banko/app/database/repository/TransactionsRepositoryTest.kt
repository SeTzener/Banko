package com.banko.app.database.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.api.services.BankoApiService
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.CreditorAccount as DaoCreditorAccount
import com.banko.app.database.Entities.DebtorAccount as DaoDebtorAccount
import com.banko.app.database.Entities.ExpenseTag as DaoExpenseTag
import com.banko.app.database.Entities.Transaction as DaoTransaction
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionsRepositoryTest {

    private lateinit var repo: TransactionsRepository
    private lateinit var db: BankoDatabase
    private val apiService = mockk<BankoApiService>(relaxed = true)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = TransactionsRepository(db, apiService)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should upsert transaction with nested creditor, debtor and expense tag`() = runBlocking {
        val creditor = DaoCreditorAccount(id = "cred-1", iban = "DE123", bban = "456")
        val debtor = DaoDebtorAccount(id = "debt-1", iban = "DE789", bban = "012")
        val tag = DaoExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        val tx = DaoTransaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = "debt-1",
            remittanceInformationUnstructured = "Nested",
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

        repo.upsertTransaction(tx, creditorAccount = creditor, debtorAccount = debtor, expenseTag = tag)

        val loaded = repo.findRawTransactionById("tx-1")
        assertNotNull(loaded)
        assertEquals("cred-1", loaded.creditorAccountId)
        assertEquals("debt-1", loaded.debtorAccountId)
        assertEquals("tag-1", loaded.expenseTagId)
    }

    @Test
    fun `should return all local transactions`() = runBlocking {
        val tx1 = DaoTransaction(
            id = "tx-1",
            bookingDate = "2024-01-15T10:30:00",
            valueDate = "2024-01-15T12:00:00",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "First",
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
        val tx2 = DaoTransaction(
            id = "tx-2",
            bookingDate = "2024-02-20T10:30:00",
            valueDate = "2024-02-20T12:00:00",
            amount = "10.00",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Second",
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

        repo.upsertTransaction(tx1)
        repo.upsertTransaction(tx2)

        val result = repo.getLocalTransactions(10).first()
        assertEquals(2, result.size)
    }

    @Test
    fun `should return only transactions within the given month`() = runBlocking {
        val janTx = DaoTransaction(
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
        val febTx = DaoTransaction(
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

        repo.upsertTransaction(janTx)
        repo.upsertTransaction(febTx)

        val result = repo.getTransactionsForMonth(LocalDateTime(2024, 1, 1, 0, 0), 2024).first()
        assertEquals(1, result.size)
        assertEquals("tx-1", result[0].id)
    }
}
