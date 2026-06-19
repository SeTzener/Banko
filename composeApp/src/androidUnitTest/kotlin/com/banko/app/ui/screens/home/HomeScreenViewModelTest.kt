package com.banko.app.ui.screens.home

import com.banko.app.domain.model.ExpenseTag as DomainExpenseTag
import com.banko.app.domain.model.Transaction as DomainTransaction
import com.banko.app.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val repository = mockk<TransactionRepository>(relaxed = true)
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

    @Test
    fun `should load cached db data on init`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        coVerify { repository.getOldestTransactions() }
        coVerify { repository.getTransactionsForDateRange(any(), any()) }
        assertEquals(YearMonth(2024, 1), vm.state.value.availableMonths.last())
    }

    @Test
    fun `should have correct default selected timespan on init`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        val timespan = vm.state.value.selectedTimespan
        assertTrue(timespan is TimespanSelection.Month)
        val month = (timespan as TimespanSelection.Month).ym
        assertEquals(month.year, vm.state.value.indicatorDateState.year)
        assertEquals(month.month, vm.state.value.indicatorDateState.monthNumber)
    }

    @Test
    fun `should query oldest transactions on init`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        coVerify { repository.getOldestTransactions() }
        assertTrue(vm.state.value.availableMonths.size >= 24)
        assertTrue(vm.state.value.availableMonths.first().year >= 2026)
    }

    @Test
    fun `should refresh data and reload transactions`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.fetchAndStoreTransactionsForDateRange(any(), any()) } returns Unit

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.Refresh)
        advanceUntilIdle()

        coVerify { repository.fetchAndStoreTransactionsForDateRange(any(), any()) }
    }

    @Test
    fun `should set isRefreshing during refresh`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.Refresh)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isRefreshing)
    }

    @Test
    fun `should delete transaction and remove from state`() = runTest(testDispatcher) {
        val domainTx = DomainTransaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Test",
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
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(domainTx))
        coEvery { repository.deleteTransaction("tx-1") } returns Unit

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.transactions.size)

        vm.handleEvent(TransactionsEvent.DeleteTransaction("tx-1"))
        advanceUntilIdle()

        coVerify { repository.deleteTransaction("tx-1") }
        assertTrue(vm.state.value.transactions.isEmpty())
    }

    @Test
    fun `should set error when delete fails`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.deleteTransaction(any()) } throws RuntimeException("Delete failed")

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.DeleteTransaction("tx-1"))
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `should select month timespan and reload transactions`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        val monthSel = TimespanSelection.Month(YearMonth(2024, 6))
        vm.handleEvent(TransactionsEvent.SelectTimespan(monthSel))
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(monthSel, state.selectedTimespan)
        assertEquals(LocalDateTime(2024, 6, 1, 0, 0), state.indicatorDateState)
        assertEquals(false, state.isYearView)
    }

    @Test
    fun `should select year timespan and reload transactions`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTimespan(TimespanSelection.Year(2025)))
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.selectedTimespan is TimespanSelection.Year)
        assertEquals(2025, (state.selectedTimespan as TimespanSelection.Year).year)
        assertTrue(state.isYearView)
    }

    @Test
    fun `should toggle from month to year and back`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isYearView)

        vm.handleEvent(TransactionsEvent.ToggleTimespanView)
        advanceUntilIdle()

        assertEquals(true, vm.state.value.isYearView)

        vm.handleEvent(TransactionsEvent.ToggleTimespanView)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isYearView)
    }

    @Test
    fun `should load more data for older month`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.LoadMore)
        advanceUntilIdle()

        coVerify { repository.fetchAndStoreTransactionsForDateRange(any(), any()) }
        assertEquals(false, vm.state.value.isLoadingMore)
    }

    @Test
    fun `should clear error when error shown event fires`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.state.value.copy(error = "Test error")

        vm.handleEvent(TransactionsEvent.ErrorShown("Test error"))
        advanceUntilIdle()

        assertNull(vm.state.value.error)
    }

    @Test
    fun `timespanState mirrors timespan changes`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        val monthSel = TimespanSelection.Month(YearMonth(2024, 6))
        vm.handleEvent(TransactionsEvent.SelectTimespan(monthSel))
        advanceUntilIdle()

        assertEquals(monthSel, vm.timespanState.value.selectedTimespan)
        assertEquals(LocalDateTime(2024, 6, 1, 0, 0), vm.timespanState.value.indicatorDateState)
        assertEquals(false, vm.timespanState.value.isYearView)
    }

    @Test
    fun `uiState mirrors error changes`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.deleteTransaction(any()) } throws RuntimeException("Delete failed")

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.DeleteTransaction("tx-1"))
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        assertEquals("Delete failed", vm.uiState.value.error)
    }

    @Test
    fun `transactionListState mirrors transaction changes`() = runTest(testDispatcher) {
        val domainTx = DomainTransaction(
            id = "tx-1",
            bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"),
            amount = 42.50,
            currency = "EUR",
            debtorAccount = null,
            remittanceInformationUnstructured = "Test",
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
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(domainTx))
        coEvery { repository.deleteTransaction("tx-1") } returns Unit

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, vm.transactionListState.value.transactions.size)

        vm.handleEvent(TransactionsEvent.DeleteTransaction("tx-1"))
        advanceUntilIdle()

        assertTrue(vm.transactionListState.value.transactions.isEmpty())
    }

    @Test
    fun `sub-state flows do not emit when unrelated fields change`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        val tsEmissions = mutableListOf<TimespanState>()

        val collectJob = vm.timespanState
            .onEach { tsEmissions.add(it) }
            .launchIn(this)
        advanceUntilIdle()
        tsEmissions.clear()

        vm.handleEvent(TransactionsEvent.DeleteTransaction("tx-1"))
        advanceUntilIdle()

        collectJob.cancel()
        advanceUntilIdle()

        assertTrue(tsEmissions.isEmpty(), "timespanState should not emit when deleting a transaction")
    }

    @Test
    fun `sub-state initial values match combined state defaults`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        val combined = vm.state.value
        assertEquals(combined.transactions, vm.transactionListState.value.transactions)
        assertEquals(combined.selectedTimespan, vm.timespanState.value.selectedTimespan)
        assertEquals(combined.error, vm.uiState.value.error)
    }

    @Test
    fun `SelectTag with same tag deselects`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag("tag-1"))
        advanceUntilIdle()
        assertEquals("tag-1", vm.state.value.selectedTagId)

        vm.handleEvent(TransactionsEvent.SelectTag("tag-1"))
        advanceUntilIdle()

        assertNull(vm.state.value.selectedTagId)
    }

    @Test
    fun `SelectTag with different tag replaces selection`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag("tag-1"))
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag("tag-2"))
        advanceUntilIdle()

        assertEquals("tag-2", vm.state.value.selectedTagId)
    }

    @Test
    fun `SelectTag with null selects uncategorized`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag(null))
        advanceUntilIdle()

        assertEquals("uncategorized", vm.state.value.selectedTagId)
    }

    @Test
    fun `toggle null tag deselects uncategorized`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag(null))
        advanceUntilIdle()
        assertEquals("uncategorized", vm.state.value.selectedTagId)

        vm.handleEvent(TransactionsEvent.SelectTag(null))
        advanceUntilIdle()

        assertNull(vm.state.value.selectedTagId)
    }

    @Test
    fun `changing month timespan resets selectedTagId`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag("tag-1"))
        advanceUntilIdle()
        assertEquals("tag-1", vm.state.value.selectedTagId)

        vm.handleEvent(TransactionsEvent.SelectTimespan(TimespanSelection.Month(YearMonth(2024, 6))))
        advanceUntilIdle()

        assertNull(vm.state.value.selectedTagId)
    }

    @Test
    fun `changing year timespan resets selectedTagId`() = runTest(testDispatcher) {
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(emptyList())

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag("tag-1"))
        advanceUntilIdle()
        assertEquals("tag-1", vm.state.value.selectedTagId)

        vm.handleEvent(TransactionsEvent.SelectTimespan(TimespanSelection.Year(2025)))
        advanceUntilIdle()

        assertNull(vm.state.value.selectedTagId)
    }

    @Test
    fun `filteredTransactionListState shows all when no tag selected`() = runTest(testDispatcher) {
        val tag1 = DomainExpenseTag(id = "tag-1", name = "Food", color = 0xFF0000FF, isEarning = false, aka = emptyList())
        val txWithTag = DomainTransaction(
            id = "tx-1", bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"), amount = 50.0, currency = "EUR",
            debtorAccount = null, remittanceInformationUnstructured = "Groceries",
            remittanceInformationUnstructuredArray = emptyList(), bankTransactionCode = "PMNT",
            internalTransactionId = "int-1", creditorName = null, creditorAccount = null,
            debtorName = null, remittanceInformationStructuredArray = null, note = null,
            expenseTag = tag1
        )
        val txNoTag = DomainTransaction(
            id = "tx-2", bookingDate = LocalDateTime.parse("2024-01-16T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-16T12:00:00"), amount = 30.0, currency = "EUR",
            debtorAccount = null, remittanceInformationUnstructured = "Other",
            remittanceInformationUnstructuredArray = emptyList(), bankTransactionCode = "PMNT",
            internalTransactionId = "int-2", creditorName = null, creditorAccount = null,
            debtorName = null, remittanceInformationStructuredArray = null, note = null,
            expenseTag = null
        )
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(txWithTag, txNoTag))

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        assertEquals(2, vm.filteredTransactionListState.value.transactions.size)
    }

    @Test
    fun `filteredTransactionListState filters by tag id`() = runTest(testDispatcher) {
        val tag1 = DomainExpenseTag(id = "tag-1", name = "Food", color = 0xFF0000FF, isEarning = false, aka = emptyList())
        val tag2 = DomainExpenseTag(id = "tag-2", name = "Transport", color = 0xFF00FF00, isEarning = false, aka = emptyList())
        val txTag1 = DomainTransaction(
            id = "tx-1", bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"), amount = 50.0, currency = "EUR",
            debtorAccount = null, remittanceInformationUnstructured = "Groceries",
            remittanceInformationUnstructuredArray = emptyList(), bankTransactionCode = "PMNT",
            internalTransactionId = "int-1", creditorName = null, creditorAccount = null,
            debtorName = null, remittanceInformationStructuredArray = null, note = null,
            expenseTag = tag1
        )
        val txTag2 = DomainTransaction(
            id = "tx-2", bookingDate = LocalDateTime.parse("2024-01-16T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-16T12:00:00"), amount = 30.0, currency = "EUR",
            debtorAccount = null, remittanceInformationUnstructured = "Bus",
            remittanceInformationUnstructuredArray = emptyList(), bankTransactionCode = "PMNT",
            internalTransactionId = "int-2", creditorName = null, creditorAccount = null,
            debtorName = null, remittanceInformationStructuredArray = null, note = null,
            expenseTag = tag2
        )
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(txTag1, txTag2))

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag("tag-1"))
        advanceUntilIdle()

        val filtered = vm.filteredTransactionListState.value
        assertEquals(1, filtered.transactions.size)
        assertEquals("tx-1", filtered.transactions.first().id)
    }

    @Test
    fun `filteredTransactionListState filters by uncategorized`() = runTest(testDispatcher) {
        val tag1 = DomainExpenseTag(id = "tag-1", name = "Food", color = 0xFF0000FF, isEarning = false, aka = emptyList())
        val txWithTag = DomainTransaction(
            id = "tx-1", bookingDate = LocalDateTime.parse("2024-01-15T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-15T12:00:00"), amount = 50.0, currency = "EUR",
            debtorAccount = null, remittanceInformationUnstructured = "Groceries",
            remittanceInformationUnstructuredArray = emptyList(), bankTransactionCode = "PMNT",
            internalTransactionId = "int-1", creditorName = null, creditorAccount = null,
            debtorName = null, remittanceInformationStructuredArray = null, note = null,
            expenseTag = tag1
        )
        val txNoTag = DomainTransaction(
            id = "tx-2", bookingDate = LocalDateTime.parse("2024-01-16T10:30:00"),
            valueDate = LocalDateTime.parse("2024-01-16T12:00:00"), amount = 30.0, currency = "EUR",
            debtorAccount = null, remittanceInformationUnstructured = "Other",
            remittanceInformationUnstructuredArray = emptyList(), bankTransactionCode = "PMNT",
            internalTransactionId = "int-2", creditorName = null, creditorAccount = null,
            debtorName = null, remittanceInformationStructuredArray = null, note = null,
            expenseTag = null
        )
        coEvery { repository.getTransactionsForDateRange(any(), any()) } returns flowOf(listOf(txWithTag, txNoTag))

        val vm = HomeScreenViewModel(repository)
        advanceUntilIdle()

        vm.handleEvent(TransactionsEvent.SelectTag(null))
        advanceUntilIdle()

        val filtered = vm.filteredTransactionListState.value
        assertEquals(1, filtered.transactions.size)
        assertEquals("tx-2", filtered.transactions.first().id)
    }
}
