package com.banko.app.api.auth

import com.banko.app.api.dto.bankoApi.AuthResponse
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

    private val apiService = mockk<BankoApiService>()
    private val tokenStorage = mockk<TokenStorage>(relaxed = true)

    private fun createRepository(): AuthRepository = AuthRepository(apiService, tokenStorage)

    @Test
    fun `login success stores tokens`() {
        coEvery { apiService.login("user@test.com", "password") } returns Result.Success(
            AuthResponse("acc-1", "access-token", "refresh-token", 900)
        )

        val repo = createRepository()
        val result = runBlocking { repo.login("user@test.com", "password") }

        assertIs<Result.Success<AuthResponse>>(result)
        verify { tokenStorage.accessToken = "access-token" }
        verify { tokenStorage.refreshToken = "refresh-token" }
        verify { tokenStorage.accountId = "acc-1" }
    }

    @Test
    fun `login dev bypass returns fake response`() {
        val repo = createRepository()
        val result = runBlocking { repo.login("dev", "dev") }

        assertIs<Result.Success<AuthResponse>>(result)
        assertEquals("dev-access-token", result.value.accessToken)
        assertEquals("dev-refresh-token", result.value.refreshToken)
        assertEquals("00000000-0000-0000-0000-000000000001", result.value.accountId)
        verify { tokenStorage.accessToken = "dev-access-token" }
        verify { tokenStorage.refreshToken = "dev-refresh-token" }
    }

    @Test
    fun `login error returns error`() {
        coEvery { apiService.login("bad@test.com", "wrong") } returns Result.Error.HttpError(
            401, "Unauthorized"
        )

        val repo = createRepository()
        val result = runBlocking { repo.login("bad@test.com", "wrong") }

        assertTrue(result is Result.Error)
    }

    @Test
    fun `register success stores tokens`() {
        coEvery {
            apiService.register("new@test.com", "password12345", "New User", true)
        } returns Result.Success(
            AuthResponse("acc-2", "access-2", "refresh-2", 900)
        )

        val repo = createRepository()
        val result = runBlocking { repo.register("new@test.com", "password12345", "New User", true) }

        assertIs<Result.Success<AuthResponse>>(result)
        verify { tokenStorage.accessToken = "access-2" }
        verify { tokenStorage.refreshToken = "refresh-2" }
        verify { tokenStorage.accountId = "acc-2" }
    }

    @Test
    fun `register error returns error`() {
        coEvery {
            apiService.register("fail@test.com", "password12345", null, false)
        } returns Result.Error.HttpError(409, "Conflict")

        val repo = createRepository()
        val result = runBlocking { repo.register("fail@test.com", "password12345", null, false) }

        assertTrue(result is Result.Error)
    }

    @Test
    fun `logout clears tokens`() {
        every { tokenStorage.clear() } just runs

        val repo = createRepository()
        repo.logout()

        verify { tokenStorage.clear() }
    }

    @Test
    fun `isLoggedIn true when access token present`() {
        every { tokenStorage.accessToken } returns "some-token"

        val repo = createRepository()
        assertTrue(repo.isLoggedIn)
    }

    @Test
    fun `isLoggedIn false when no access token`() {
        every { tokenStorage.accessToken } returns null

        val repo = createRepository()
        assertTrue(!repo.isLoggedIn)
    }

    @Test
    fun `refresh token success stores new tokens`() {
        every { tokenStorage.refreshToken } returns "old-refresh"
        coEvery { apiService.refreshToken("old-refresh") } returns Result.Success(
            AuthResponse("acc-1", "new-access", "new-refresh", 900)
        )

        val repo = createRepository()
        val result = runBlocking { repo.refreshToken() }

        assertIs<Result.Success<AuthResponse>>(result)
        verify { tokenStorage.accessToken = "new-access" }
        verify { tokenStorage.refreshToken = "new-refresh" }
    }

    @Test
    fun `refresh token error clears tokens`() {
        every { tokenStorage.refreshToken } returns "expired-refresh"
        coEvery { apiService.refreshToken("expired-refresh") } returns Result.Error.HttpError(
            401, "Unauthorized"
        )

        val repo = createRepository()
        val result = runBlocking { repo.refreshToken() }

        assertTrue(result is Result.Error)
        verify { tokenStorage.clear() }
    }

    @Test
    fun `refresh token with no stored token returns error`() {
        every { tokenStorage.refreshToken } returns null

        val repo = createRepository()
        val result = runBlocking { repo.refreshToken() }

        assertTrue(result is Result.Error.UnexpectedError)
    }
}
