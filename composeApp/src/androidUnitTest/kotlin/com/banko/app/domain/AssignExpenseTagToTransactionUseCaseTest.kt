package com.banko.app.domain

import com.banko.app.database.Entities.ExpenseTag
import com.banko.app.database.Entities.Transaction
import com.banko.app.database.repository.ExpenseTagRepository
import com.banko.app.database.repository.TransactionsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertFailsWith

class AssignExpenseTagToTransactionUseCaseTest {

    private val transactionRepository = mockk<TransactionsRepository>()
    private val expenseTagRepository = mockk<ExpenseTagRepository>()
    private val useCase = AssignExpenseTagToTransactionUseCase(
        transactionRepository = transactionRepository,
        expenseTagRepository = expenseTagRepository
    )

    @Test
    fun `should assign expense tag to transaction`() = runBlocking {
        val transactionId = "tx-1"
        val expenseTagId = "tag-1"
        val transaction = Transaction(
            id = transactionId,
            bookingDate = "2024-01-15",
            valueDate = "2024-01-15",
            amount = "42.50",
            currency = "EUR",
            debtorAccountId = null,
            remittanceInformationUnstructured = "Test payment",
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
        val tag = ExpenseTag(
            id = expenseTagId,
            name = "Groceries",
            color = 0xFF00FF00,
            isEarning = false,
            aka = emptyList()
        )

        coEvery { transactionRepository.findRawTransactionById(transactionId) } returns transaction
        coEvery { expenseTagRepository.findExpenseTagById(expenseTagId) } returns flowOf(tag)
        coEvery { transactionRepository.upsertTransaction(any(), any(), any(), any()) } returns Unit

        useCase(transactionId, expenseTagId)

        coVerify {
            transactionRepository.findRawTransactionById(transactionId)
            expenseTagRepository.findExpenseTagById(expenseTagId)
            transactionRepository.upsertTransaction(
                transaction = transaction.copy(expenseTagId = tag.id),
                creditorAccount = null,
                debtorAccount = null,
                expenseTag = tag
            )
        }
    }

    @Test
    fun `should throw when transaction not found`() = runBlocking {
        coEvery { transactionRepository.findRawTransactionById(any()) } returns null

        assertFailsWith<IllegalStateException> {
            useCase("nonexistent", "tag-1")
        }
        Unit
    }

    @Test
    fun `should throw when expense tag not found`() = runBlocking {
        coEvery { transactionRepository.findRawTransactionById(any()) } returns mockk()
        coEvery { expenseTagRepository.findExpenseTagById(any()) } returns flowOf(null)

        assertFailsWith<IllegalStateException> {
            useCase("tx-1", "nonexistent")
        }
        Unit
    }
}
