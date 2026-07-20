package com.banko.app.ui.screens.login

import com.banko.app.api.auth.SessionManager
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val sessionManager = mockk<SessionManager>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields`() {
        val viewModel = LoginViewModel(sessionManager)

        val state = viewModel.state.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onEmailChanged updates email and clears error`() {
        val viewModel = LoginViewModel(sessionManager)
        viewModel.login()
        assertNotNull(viewModel.state.value.error)

        viewModel.onEmailChanged("test@example.com")

        assertEquals("test@example.com", viewModel.state.value.email)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `onPasswordChanged updates password and clears error`() {
        val viewModel = LoginViewModel(sessionManager)
        viewModel.login()
        assertNotNull(viewModel.state.value.error)

        viewModel.onPasswordChanged("mypassword")

        assertEquals("mypassword", viewModel.state.value.password)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `login with blank email shows error`() {
        val viewModel = LoginViewModel(sessionManager)
        viewModel.state.value.let { state ->
            assertEquals("", state.email)
        }

        viewModel.login()

        assertEquals("Please fill in all fields.", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `login with blank password shows error`() {
        val viewModel = LoginViewModel(sessionManager)
        viewModel.onEmailChanged("test@example.com")

        viewModel.login()

        assertEquals("Please fill in all fields.", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `login with valid fields calls sessionManager login`() = runTest {
        val viewModel = LoginViewModel(sessionManager)
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isLoading)
        verify { sessionManager.login("test@example.com", "password123") }
    }
}
