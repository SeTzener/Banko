package com.banko.app.api.auth

import com.banko.app.api.dto.bankoApi.AuthResponse
import com.banko.app.api.utils.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = mockk<AuthRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init authenticated when logged in`() {
        every { authRepository.isLoggedIn } returns true

        val sessionManager = SessionManager(authRepository)

        assertEquals(AuthState.Authenticated, sessionManager.authState.value)
    }

    @Test
    fun `init unauthenticated when not logged in`() {
        every { authRepository.isLoggedIn } returns false

        val sessionManager = SessionManager(authRepository)

        assertEquals(AuthState.Unauthenticated, sessionManager.authState.value)
    }

    @Test
    fun `login success transitions to authenticated`() = runTest(testDispatcher) {
        every { authRepository.isLoggedIn } returns false
        coEvery { authRepository.login("a@b.com", "pw") } returns Result.Success(
            AuthResponse("acc-1", "tok", "ref", 900)
        )
        val sessionManager = SessionManager(authRepository)

        sessionManager.login("a@b.com", "pw")
        advanceUntilIdle()

        assertEquals(AuthState.Authenticated, sessionManager.authState.value)
    }

    @Test
    fun `login error transitions to unauthenticated`() = runTest(testDispatcher) {
        every { authRepository.isLoggedIn } returns false
        coEvery { authRepository.login("a@b.com", "wrong") } returns Result.Error.HttpError(
            401, "Unauthorized"
        )
        val sessionManager = SessionManager(authRepository)

        sessionManager.login("a@b.com", "wrong")
        advanceUntilIdle()

        assertEquals(AuthState.Unauthenticated, sessionManager.authState.value)
    }

    @Test
    fun `login sets loading then authenticated`() = runTest(testDispatcher) {
        every { authRepository.isLoggedIn } returns false
        coEvery { authRepository.login("a@b.com", "pw") } coAnswers {
            delay(100)
            Result.Success(AuthResponse("acc-1", "tok", "ref", 900))
        }
        val sessionManager = SessionManager(authRepository)

        sessionManager.login("a@b.com", "pw")
        advanceUntilIdle()
        assertEquals(AuthState.Authenticated, sessionManager.authState.value)
    }

    @Test
    fun `logout transitions to unauthenticated`() {
        every { authRepository.isLoggedIn } returns true
        val sessionManager = SessionManager(authRepository)

        sessionManager.logout()

        assertEquals(AuthState.Unauthenticated, sessionManager.authState.value)
        verify { authRepository.logout() }
    }

    @Test
    fun `register success transitions to authenticated`() = runTest(testDispatcher) {
        every { authRepository.isLoggedIn } returns false
        coEvery { authRepository.register("a@b.com", "pw12345678", "Name", true) } returns Result.Success(
            AuthResponse("acc-1", "tok", "ref", 900)
        )
        val sessionManager = SessionManager(authRepository)

        sessionManager.register("a@b.com", "pw12345678", "Name", true)
        advanceUntilIdle()

        assertEquals(AuthState.Authenticated, sessionManager.authState.value)
    }

    @Test
    fun `register error transitions to unauthenticated`() = runTest(testDispatcher) {
        every { authRepository.isLoggedIn } returns false
        coEvery { authRepository.register("a@b.com", "pw12345678", null, false) } returns Result.Error.HttpError(
            409, "Conflict"
        )
        val sessionManager = SessionManager(authRepository)

        sessionManager.register("a@b.com", "pw12345678", null, false)
        advanceUntilIdle()

        assertEquals(AuthState.Unauthenticated, sessionManager.authState.value)
    }
}
