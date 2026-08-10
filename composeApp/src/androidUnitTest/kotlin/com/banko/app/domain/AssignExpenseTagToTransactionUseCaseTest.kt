package com.banko.app.domain

import com.banko.app.data.repository.ExpenseTagRepository
import com.banko.app.data.repository.TransactionRepository
import com.banko.app.domain.model.ExpenseTag
import com.banko.app.domain.model.Transaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.test.assertFailsWith

class AssignExpenseTagToTransactionUseCaseTest {

    private val transactionRepository = mockk<TransactionRepository>()
    private val expenseTagRepository = mockk<ExpenseTagRepository>()
    private val useCase = AssignExpenseTagToTransactionUseCase(
        transactionRepository = transactionRepository,
        expenseTagRepository = expenseTagRepository
    )

    private fun transaction(id: String, expenseTagId: String? = null) = Transaction(
        id = id,
        bookingDate = LocalDateTime(2024, 1, 15, 10, 30, 0),
        valueDate = LocalDateTime(2024, 1, 15, 10, 30, 0),
        amount = 42.50,
        currency = "EUR",
        debtorAccount = null,
        remittanceInformationUnstructured = "Test payment",
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

    @Test
    fun `should assign expense tag to transaction`() = runBlocking {
        val transactionId = "tx-1"
        val expenseTagId = "tag-1"
        val tag = ExpenseTag(id = expenseTagId, name = "Groceries", color = 0xFF00FF, isEarning = false, aka = emptyList())

        coEvery { transactionRepository.getTransactionById(transactionId) } returns transaction(transactionId)
        coEvery { expenseTagRepository.getExpenseTagById(expenseTagId) } returns tag
        coEvery { transactionRepository.assignExpenseTag(transactionId, expenseTagId) } returns Unit

        useCase(transactionId, expenseTagId)

        coVerify {
            transactionRepository.getTransactionById(transactionId)
            expenseTagRepository.getExpenseTagById(expenseTagId)
            transactionRepository.assignExpenseTag(transactionId, expenseTagId)
        }
    }

    @Test
    fun `should throw when transaction not found`() = runBlocking {
        coEvery { transactionRepository.getTransactionById(any()) } returns null

        assertFailsWith<IllegalStateException> {
            useCase("nonexistent", "tag-1")
        }
        Unit
    }

    @Test
    fun `should throw when expense tag not found`() = runBlocking {
        coEvery { transactionRepository.getTransactionById(any()) } returns transaction("tx-1")
        coEvery { expenseTagRepository.getExpenseTagById(any()) } returns null

        assertFailsWith<IllegalStateException> {
            useCase("tx-1", "nonexistent")
        }
        Unit
    }

    @Test
    fun `should clear expense tag when expenseTagId is null`() = runBlocking {
        val transactionId = "tx-1"
        coEvery { transactionRepository.getTransactionById(transactionId) } returns transaction(transactionId)
        coEvery { transactionRepository.assignExpenseTag(transactionId, null) } returns Unit

        useCase(transactionId, null)

        coVerify {
            transactionRepository.getTransactionById(transactionId)
            transactionRepository.assignExpenseTag(transactionId, null)
        }
        coVerify(exactly = 0) {
            expenseTagRepository.getExpenseTagById(any())
        }
    }
}
