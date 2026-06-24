package com.banko.app.ui.screens.profile

import com.banko.app.api.auth.SessionManager
import com.banko.app.api.auth.TokenStorage
import com.banko.app.api.dto.bankoApi.UserProfileResponse
import com.banko.app.api.dto.bankoApi.UserExportData
import com.banko.app.api.dto.bankoApi.ConsentLogEntry
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tokenStorage = mockk<TokenStorage>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiService = mockk<BankoApiService>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `init loads profile on creation`() = runTest(testDispatcher) {
        val testData = UserProfileResponse(
            accountId = "acc-1",
            email = "user@test.com",
            fullName = "Test User",
            consentGiven = true,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
        )
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Success(testData)

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)

        assertEquals("acc-1", viewModel.state.value.accountId)
    }

    @Test
    fun `loadProfile success updates state`() = runTest(testDispatcher) {
        val testData = UserProfileResponse(
            accountId = "acc-1",
            email = "user@test.com",
            fullName = "Test User",
            consentGiven = true,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
        )
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Success(testData)

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(testData, state.profile)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadProfile error sets error state`() = runTest(testDispatcher) {
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Error.HttpError(404, "Not Found")

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.profile)
        assertEquals(false, state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `exportData success stores export data`() = runTest(testDispatcher) {
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Success(
            UserProfileResponse("acc-1", "u@t.com", createdAt = "", updatedAt = "", consentGiven = true)
        )
        coEvery { apiService.exportData() } returns Result.Success(
            UserExportData(
                accountId = "acc-1",
                email = "u@t.com",
                consentGiven = true,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z",
                consentLogs = listOf(
                    ConsentLogEntry("1.0", "Policy v1", true, "2024-01-01T00:00:00Z")
                )
            )
        )

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        advanceUntilIdle()

        viewModel.exportData()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.exportData)
        assertEquals(1, state.exportData!!.consentLogs.size)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `exportData error sets error`() = runTest(testDispatcher) {
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Success(
            UserProfileResponse("acc-1", "u@t.com", createdAt = "", updatedAt = "", consentGiven = true)
        )
        coEvery { apiService.exportData() } returns Result.Error.HttpError(500, "Server Error")

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        advanceUntilIdle()

        viewModel.exportData()
        advanceUntilIdle()

        assertNull(viewModel.state.value.exportData)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `acceptConsent calls api and reloads profile`() = runTest(testDispatcher) {
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Success(
            UserProfileResponse("acc-1", "u@t.com", createdAt = "", updatedAt = "", consentGiven = true)
        )
        coEvery { apiService.acceptConsent(any()) } returns Result.Success(Unit)

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        advanceUntilIdle()

        viewModel.acceptConsent("policy-1")
        advanceUntilIdle()

        coVerify { apiService.acceptConsent("policy-1") }
        coVerify(atLeast = 2) { apiService.getProfile() }
    }

    @Test
    fun `deleteAccount success clears tokens and logs out`() = runTest(testDispatcher) {
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Success(
            UserProfileResponse("acc-1", "u@t.com", createdAt = "", updatedAt = "", consentGiven = true)
        )
        coEvery { apiService.deleteAccount() } returns Result.Success(Unit)

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        advanceUntilIdle()

        viewModel.deleteAccount()
        advanceUntilIdle()

        verify { tokenStorage.clear() }
        verify { sessionManager.logout() }
    }

    @Test
    fun `deleteAccount error sets error`() = runTest(testDispatcher) {
        every { tokenStorage.accountId } returns "acc-1"
        coEvery { apiService.getProfile() } returns Result.Success(
            UserProfileResponse("acc-1", "u@t.com", createdAt = "", updatedAt = "", consentGiven = true)
        )
        coEvery { apiService.deleteAccount() } returns Result.Error.HttpError(404, "Not Found")

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        advanceUntilIdle()

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `logout calls sessionManager logout`() {
        every { tokenStorage.accountId } returns "acc-1"

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        viewModel.logout()

        verify { sessionManager.logout() }
    }

    @Test
    fun `clearExportData clears export data from state`() {
        every { tokenStorage.accountId } returns "acc-1"

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        viewModel.state.value.let { state ->
            assertNull(state.exportData)
        }
    }

    @Test
    fun `clearError clears error from state`() {
        every { tokenStorage.accountId } returns "acc-1"

        val viewModel = ProfileViewModel(tokenStorage, sessionManager, apiService)
        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }
}
