package com.banko.app.ui.screens.settings

import androidx.compose.ui.graphics.Color
import com.banko.app.api.repositories.ExpenseTagRepository
import com.banko.app.database.Entities.ExpenseTag as DaoExpenseTag
import com.banko.app.domain.CurrencyPreferences
import com.banko.app.database.repository.ExpenseTagRepository as DatabaseExpenseTagRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {

    private val dbRepository = mockk<DatabaseExpenseTagRepository>(relaxed = true)
    private val apiRepository = mockk<ExpenseTagRepository>(relaxed = true)
    private val currencyPreferences = mockk<CurrencyPreferences>(relaxed = true)
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { dbRepository.getAllExpenseTags() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should load expense tags from db on init`() = runTest(testDispatcher) {
        val vm = SettingsScreenViewModel(
            dbRepository = dbRepository,
            apiRepository = apiRepository,
            currencyPreferences = currencyPreferences
        )
        advanceUntilIdle()

        coVerify { dbRepository.getAllExpenseTags() }
        assertEquals(emptyList<ExpenseTag>(), vm.screenState.value.expenseTags)
    }

    @Test
    fun `should load expense tags from API and upsert into DB`() = runTest(testDispatcher) {
        val apiTags = listOf(
            ExpenseTag(id = "1", name = "Food", color = Color.Red, isEarning = false, aka = emptyList())
        )
        every { currencyPreferences.selectedCurrency } returns flowOf("NOK")
        coEvery { apiRepository.getExpenseTags() } returns apiTags

        val vm = SettingsScreenViewModel(
            dbRepository = dbRepository,
            apiRepository = apiRepository,
            currencyPreferences = currencyPreferences
        )
        advanceUntilIdle()

        vm.loadExpenseTags()
        advanceUntilIdle()

        coVerify {
            apiRepository.getExpenseTags()
            dbRepository.upsertExpenseTag(any())
        }
    }

    @Test
    fun `should update expense tag via API then upsert locally`() = runTest(testDispatcher) {
        val updatedTag = ExpenseTag(id = "1", name = "Transport", color = Color.Blue, isEarning = false, aka = emptyList())
        coEvery { apiRepository.updateExpenseTag(any()) } returns updatedTag

        val vm = SettingsScreenViewModel(
            dbRepository = dbRepository,
            apiRepository = apiRepository,
            currencyPreferences = currencyPreferences
        )
        advanceUntilIdle()

        vm.updateExpenseTag(updatedTag)
        advanceUntilIdle()

        coVerify {
            apiRepository.updateExpenseTag(updatedTag)
            dbRepository.upsertExpenseTag(any())
        }
    }

    @Test
    fun `should not upsert locally when API update throws`() = runTest(testDispatcher) {
        coEvery { apiRepository.updateExpenseTag(any()) } throws RuntimeException("API error")

        val vm = SettingsScreenViewModel(
            dbRepository = dbRepository,
            apiRepository = apiRepository,
            currencyPreferences = currencyPreferences
        )
        advanceUntilIdle()

        vm.updateExpenseTag(
            ExpenseTag(id = "1", name = "Transport", color = Color.Blue, isEarning = false, aka = emptyList())
        )
        advanceUntilIdle()

        coVerify(exactly = 0) { dbRepository.upsertExpenseTag(any()) }
    }

    @Test
    fun `should create expense tag via API then upsert locally`() = runTest(testDispatcher) {
        val createdTag = ExpenseTag(id = "new-1", name = "Shopping", color = Color.Green, isEarning = false, aka = emptyList())
        coEvery { apiRepository.createExpenseTag(any(), any(), any()) } returns createdTag

        val vm = SettingsScreenViewModel(
            dbRepository = dbRepository,
            apiRepository = apiRepository,
            currencyPreferences = currencyPreferences
        )
        advanceUntilIdle()

        vm.createExpenseTag("Shopping", Color.Green, false)
        advanceUntilIdle()

        coVerify {
            apiRepository.createExpenseTag("Shopping", any(), false)
            dbRepository.upsertExpenseTag(any())
        }
    }

    @Test
    fun `should not upsert locally when API create throws`() = runTest(testDispatcher) {
        coEvery { apiRepository.createExpenseTag(any(), any(), any()) } throws RuntimeException("API error")

        val vm = SettingsScreenViewModel(
            dbRepository = dbRepository,
            apiRepository = apiRepository,
            currencyPreferences = currencyPreferences
        )
        advanceUntilIdle()

        vm.createExpenseTag("Shopping", Color.Green, false)
        advanceUntilIdle()

        coVerify(exactly = 0) { dbRepository.upsertExpenseTag(any()) }
    }

    @Test
    fun `should delete expense tag via API then remove locally`() = runTest(testDispatcher) {
        val vm = SettingsScreenViewModel(
            dbRepository = dbRepository,
            apiRepository = apiRepository,
            currencyPreferences = currencyPreferences
        )
        advanceUntilIdle()

        vm.deleteExpenseTag("tag-1")
        advanceUntilIdle()

        coVerify {
            apiRepository.deleteExpenseTag("tag-1")
            dbRepository.deleteExpenseTag("tag-1")
        }
    }
}
