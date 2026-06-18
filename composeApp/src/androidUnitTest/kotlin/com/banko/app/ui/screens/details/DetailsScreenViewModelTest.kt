package com.banko.app.ui.screens.details

import com.banko.app.api.repositories.ExpenseTagRepository
import com.banko.app.api.utils.Result
import com.banko.app.database.Entities.ExpenseTag as DaoExpenseTag
import com.banko.app.domain.AssignExpenseTagToTransactionUseCase
import com.banko.app.domain.GetAllExpenseTagUseCase
import com.banko.app.domain.SaveNoteUseCase
import com.banko.app.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsScreenViewModelTest {

    private val apiTagRepository = mockk<ExpenseTagRepository>(relaxed = true)
    private val updateTransactionUseCase = mockk<AssignExpenseTagToTransactionUseCase>(relaxed = true)
    private val getExpenseTagsUseCase = mockk<GetAllExpenseTagUseCase>(relaxed = true)
    private val saveNoteUseCase = mockk<SaveNoteUseCase>(relaxed = true)
    private val transactionRepository = mockk<TransactionRepository>(relaxed = true)
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should load expense tags on init`() = runTest(testDispatcher) {
        val tags = listOf(
            DaoExpenseTag(id = "1", name = "Food", color = 0xFF0000, isEarning = false, aka = null)
        )
        every { getExpenseTagsUseCase.invoke() } returns flowOf(tags)

        val vm = DetailsScreenViewModel(
            apiTagRepository = apiTagRepository,
            updateTransactionUseCase = updateTransactionUseCase,
            getExpenseTags = getExpenseTagsUseCase,
            saveNoteUseCase = saveNoteUseCase,
            transactionRepository = transactionRepository
        )
        advanceUntilIdle()

        assertEquals(1, vm.screenState.value.expenseTags.size)
    }

    @Test
    fun `should assign expense tag via API then update locally`() = runTest(testDispatcher) {
        val vm = DetailsScreenViewModel(
            apiTagRepository = apiTagRepository,
            updateTransactionUseCase = updateTransactionUseCase,
            getExpenseTags = getExpenseTagsUseCase,
            saveNoteUseCase = saveNoteUseCase,
            transactionRepository = transactionRepository
        )
        advanceUntilIdle()

        vm.assignExpenseTag("tx-1", "tag-1")
        advanceUntilIdle()

        coVerify {
            apiTagRepository.assignExpenseTag("tx-1", "tag-1")
            updateTransactionUseCase.invoke(transactionId = "tx-1", expenseTagId = "tag-1")
        }
    }

    @Test
    fun `should roll back tag assignment on API failure`() = runTest(testDispatcher) {
        coEvery { apiTagRepository.assignExpenseTag(any(), any()) } throws RuntimeException("API error")

        val vm = DetailsScreenViewModel(
            apiTagRepository = apiTagRepository,
            updateTransactionUseCase = updateTransactionUseCase,
            getExpenseTags = getExpenseTagsUseCase,
            saveNoteUseCase = saveNoteUseCase,
            transactionRepository = transactionRepository
        )
        advanceUntilIdle()

        vm.assignExpenseTag("tx-1", "tag-1")
        advanceUntilIdle()

        coVerify {
            apiTagRepository.assignExpenseTag("tx-1", "tag-1")
            updateTransactionUseCase.invoke(transactionId = "tx-1", expenseTagId = "tag-1")
        }
    }

    @Test
    fun `should save note via use case`() = runTest(testDispatcher) {
        val vm = DetailsScreenViewModel(
            apiTagRepository = apiTagRepository,
            updateTransactionUseCase = updateTransactionUseCase,
            getExpenseTags = getExpenseTagsUseCase,
            saveNoteUseCase = saveNoteUseCase,
            transactionRepository = transactionRepository
        )
        advanceUntilIdle()

        vm.saveNote("Test note", "tx-1")
        advanceUntilIdle()

        coVerify { saveNoteUseCase.invoke(id = "tx-1", note = "Test note") }
    }

    @Test
    fun `should delete transaction via repository`() = runTest(testDispatcher) {
        val vm = DetailsScreenViewModel(
            apiTagRepository = apiTagRepository,
            updateTransactionUseCase = updateTransactionUseCase,
            getExpenseTags = getExpenseTagsUseCase,
            saveNoteUseCase = saveNoteUseCase,
            transactionRepository = transactionRepository
        )
        advanceUntilIdle()

        vm.deleteTransaction("tx-1")
        advanceUntilIdle()

        coVerify { transactionRepository.deleteTransaction("tx-1") }
    }

    @Test
    fun `should not set error when delete succeeds`() = runTest(testDispatcher) {
        coEvery { transactionRepository.deleteTransaction(any()) } returns Unit

        val vm = DetailsScreenViewModel(
            apiTagRepository = apiTagRepository,
            updateTransactionUseCase = updateTransactionUseCase,
            getExpenseTags = getExpenseTagsUseCase,
            saveNoteUseCase = saveNoteUseCase,
            transactionRepository = transactionRepository
        )
        advanceUntilIdle()

        vm.deleteTransaction("tx-1")
        advanceUntilIdle()

        assertNull(vm.screenState.value.error)
    }
}
