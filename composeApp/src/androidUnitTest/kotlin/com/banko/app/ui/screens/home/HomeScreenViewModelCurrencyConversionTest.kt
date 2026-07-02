package com.banko.app.ui.screens.home

import com.banko.app.data.repository.CurrencyRepository
import com.banko.app.data.repository.TransactionRepository
import com.banko.app.domain.CurrencyPreferences
import com.banko.app.domain.model.Transaction as DomainTransaction
import com.banko.app.ui.models.toUi
import io.mockk.coEvery
import io.mockk.coVerify
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.math.roundToLong
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelCurrencyConversionTest {

    private val repository = mockk<TransactionRepository>(relaxed = true)
    private val currencyRepository = mockk<CurrencyRepository>(relaxed = true)
    private val currencyPreferences = mockk<CurrencyPreferences>(relaxed = true)
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getOldestTransactions() } returns LocalDateTime(2024, 1, 1, 0, 0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTransaction(
        id: String,
        amount: Double,
        currency: String,
        date: LocalDate = LocalDate(2026, 1, 15),
    ): DomainTransaction {
        val dateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 10, 0)
        return DomainTransaction(
            id = id,
            bookingDate = dateTime,
            valueDate = dateTime,
            amount = amount,
            currency = currency,
            remittanceInformationUnstructured = "Test $id",
            remittanceInformationUnstructuredArray = emptyList(),
            bankTransactionCode = "PMNT",
            internalTransactionId = "int-$id",
        )
    }

    @Test
    fun `should convert amounts from foreign currency to selected currency`() = runTest(testDispatcher) {
        val tx = createTransaction("tx-1", 100.0, "USD")
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(tx))
        coEvery { currencyPreferences.selectedCurrency } returns flowOf("EUR")
        coEvery {
            currencyRepository.getRatesForDateRange("USD", "EUR", any(), any())
        } returns mapOf(LocalDate(2026, 1, 15) to 0.92)

        val vm = HomeScreenViewModel(repository, currencyRepository, currencyPreferences)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.transactions.size)
        val expected = (100.0 * 0.92 * 100.0).roundToLong() / 100.0
        assertEquals(expected, state.transactions[0].amount)
        assertEquals("EUR", state.transactions[0].currency)
    }

    @Test
    fun `should not convert transactions already in selected currency`() = runTest(testDispatcher) {
        val tx = createTransaction("tx-1", 100.0, "EUR")
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(tx))
        coEvery { currencyPreferences.selectedCurrency } returns flowOf("EUR")

        val vm = HomeScreenViewModel(repository, currencyRepository, currencyPreferences)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.transactions.size)
        assertEquals(100.0, state.transactions[0].amount)
        assertEquals("EUR", state.transactions[0].currency)
    }

    @Test
    fun `should skip conversion for unsupported currencies`() = runTest(testDispatcher) {
        val tx = createTransaction("tx-1", 100.0, "XYZ")
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(tx))
        coEvery { currencyPreferences.selectedCurrency } returns flowOf("EUR")

        val vm = HomeScreenViewModel(repository, currencyRepository, currencyPreferences)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.transactions.size)
        assertEquals(100.0, state.transactions[0].amount)
        assertEquals("XYZ", state.transactions[0].currency)
    }

    @Test
    fun `should leave amount unchanged when rate is missing`() = runTest(testDispatcher) {
        val tx = createTransaction("tx-1", 100.0, "USD")
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(tx))
        coEvery { currencyPreferences.selectedCurrency } returns flowOf("EUR")
        coEvery {
            currencyRepository.getRatesForDateRange("USD", "EUR", any(), any())
        } returns mapOf(LocalDate(2026, 1, 16) to 0.92)

        val vm = HomeScreenViewModel(repository, currencyRepository, currencyPreferences)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.transactions.size)
        assertEquals(100.0, state.transactions[0].amount)
        assertEquals("USD", state.transactions[0].currency)
    }

    @Test
    fun `should handle multiple transactions with different currencies`() = runTest(testDispatcher) {
        val txUsd = createTransaction("tx-1", 100.0, "USD")
        val txGbp = createTransaction("tx-2", 200.0, "GBP")
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(txUsd, txGbp))
        coEvery { currencyPreferences.selectedCurrency } returns flowOf("EUR")
        coEvery {
            currencyRepository.getRatesForDateRange("USD", "EUR", any(), any())
        } returns mapOf(LocalDate(2026, 1, 15) to 0.92)
        coEvery {
            currencyRepository.getRatesForDateRange("GBP", "EUR", any(), any())
        } returns mapOf(LocalDate(2026, 1, 15) to 1.17)

        val vm = HomeScreenViewModel(repository, currencyRepository, currencyPreferences)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(2, state.transactions.size)
        val expectedUsd = (100.0 * 0.92 * 100.0).roundToLong() / 100.0
        val expectedGbp = (200.0 * 1.17 * 100.0).roundToLong() / 100.0
        assertEquals(expectedUsd, state.transactions[0].amount)
        assertEquals(expectedGbp, state.transactions[1].amount)
        assertEquals("EUR", state.transactions[0].currency)
        assertEquals("EUR", state.transactions[1].currency)
    }

    @Test
    fun `should round converted amounts to 2 decimal places`() = runTest(testDispatcher) {
        val tx = createTransaction("tx-1", 33.33, "USD")
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(tx))
        coEvery { currencyPreferences.selectedCurrency } returns flowOf("EUR")
        coEvery {
            currencyRepository.getRatesForDateRange("USD", "EUR", any(), any())
        } returns mapOf(LocalDate(2026, 1, 15) to 0.9234567)

        val vm = HomeScreenViewModel(repository, currencyRepository, currencyPreferences)
        advanceUntilIdle()

        val state = vm.state.value
        val converted = state.transactions[0].amount
        val decimalPlaces = converted.toString().substringAfter(".", "").length
        assert(decimalPlaces <= 2 || converted % 0.01 == 0.0)
    }

    @Test
    fun `should use booking date for rate lookup`() = runTest(testDispatcher) {
        val tx = createTransaction("tx-1", 100.0, "USD", date = LocalDate(2026, 7, 15))
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(tx))
        coEvery { currencyPreferences.selectedCurrency } returns flowOf("EUR")
        coEvery {
            currencyRepository.getRatesForDateRange("USD", "EUR", LocalDate(2026, 7, 1), LocalDate(2026, 7, 31))
        } returns mapOf(LocalDate(2026, 7, 15) to 0.91)

        val vm = HomeScreenViewModel(repository, currencyRepository, currencyPreferences)
        advanceUntilIdle()

        coVerify {
            currencyRepository.getRatesForDateRange("USD", "EUR", LocalDate(2026, 7, 1), LocalDate(2026, 7, 31))
        }
        assertEquals("EUR", vm.state.value.transactions[0].currency)
    }
}
