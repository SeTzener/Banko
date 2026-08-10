package com.banko.app.ui.screens.settings

import androidx.compose.ui.graphics.Color
import com.banko.app.api.services.BankoApiService
import com.banko.app.data.repository.ExpenseTagRepository
import com.banko.app.domain.CurrencyPreferences
import com.banko.app.ui.models.ExpenseTag
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {

    private val expenseTagRepository = mockk<ExpenseTagRepository>(relaxed = true)
    private val currencyPreferences = mockk<CurrencyPreferences>(relaxed = true)
    private val apiService = mockk<BankoApiService>(relaxed = true)
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { expenseTagRepository.getAllExpenseTags() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsScreenViewModel(
        expenseTagRepository = expenseTagRepository,
        currencyPreferences = currencyPreferences,
        apiService = apiService,
    )

    @Test
    fun `should load expense tags from db on init`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        coVerify { expenseTagRepository.getAllExpenseTags() }
        assertEquals(emptyList<ExpenseTag>(), vm.screenState.value.expenseTags)
    }

    @Test
    fun `should refresh expense tags from API on loadExpenseTags`() = runTest(testDispatcher) {
        every { currencyPreferences.selectedCurrency } returns flowOf("NOK")
        coEvery { expenseTagRepository.refreshExpenseTags() } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadExpenseTags()
        advanceUntilIdle()

        coVerify { expenseTagRepository.refreshExpenseTags() }
        assertNull(vm.screenState.value.error)
    }

    @Test
    fun `should set error when refresh fails`() = runTest(testDispatcher) {
        coEvery { expenseTagRepository.refreshExpenseTags() } throws RuntimeException("Network error")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadExpenseTags()
        advanceUntilIdle()

        assertNotNull(vm.screenState.value.error)
    }

    @Test
    fun `should update expense tag via repository`() = runTest(testDispatcher) {
        val updatedTag = ExpenseTag(id = "1", name = "Transport", color = Color.Blue, isEarning = false, aka = emptyList())
        coEvery { expenseTagRepository.updateExpenseTag(any()) } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateExpenseTag(updatedTag)
        advanceUntilIdle()

        coVerify { expenseTagRepository.updateExpenseTag(any()) }
        assertNull(vm.screenState.value.error)
    }

    @Test
    fun `should set error when API update throws`() = runTest(testDispatcher) {
        coEvery { expenseTagRepository.updateExpenseTag(any()) } throws RuntimeException("API error")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateExpenseTag(
            ExpenseTag(id = "1", name = "Transport", color = Color.Blue, isEarning = false, aka = emptyList())
        )
        advanceUntilIdle()

        assertNotNull(vm.screenState.value.error)
    }

    @Test
    fun `should create expense tag via repository`() = runTest(testDispatcher) {
        coEvery { expenseTagRepository.createExpenseTag(any(), any(), any()) } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.createExpenseTag("Shopping", Color.Green, false)
        advanceUntilIdle()

        coVerify { expenseTagRepository.createExpenseTag("Shopping", any(), false) }
        assertNull(vm.screenState.value.error)
    }

    @Test
    fun `should set error when API create throws`() = runTest(testDispatcher) {
        coEvery { expenseTagRepository.createExpenseTag(any(), any(), any()) } throws RuntimeException("API error")

        val vm = createViewModel()
        advanceUntilIdle()

        vm.createExpenseTag("Shopping", Color.Green, false)
        advanceUntilIdle()

        assertNotNull(vm.screenState.value.error)
    }

    @Test
    fun `should delete expense tag via repository`() = runTest(testDispatcher) {
        coEvery { expenseTagRepository.deleteExpenseTag(any()) } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.deleteExpenseTag("tag-1")
        advanceUntilIdle()

        coVerify { expenseTagRepository.deleteExpenseTag("tag-1") }
        assertNull(vm.screenState.value.error)
    }
}
