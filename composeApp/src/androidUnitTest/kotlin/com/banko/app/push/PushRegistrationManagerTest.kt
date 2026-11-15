package com.banko.app.push

import com.banko.app.api.auth.TokenStorage
import com.banko.app.api.dto.bankoApi.RegisterDeviceResponse
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushRegistrationManagerTest {

    private val apiService = mockk<BankoApiService>()
    private val tokenStorage = mockk<TokenStorage>()
    private val pushStorage = mockk<PushStorage>(relaxed = true)
    private val tokenBridge = PushTokenBridge()

    @Test
    fun `registers token when authenticated`() = runTest {
        tokenBridge.update("token-1")
        every { tokenStorage.accessToken } returns "access-token"
        coEvery { apiService.registerDevice("token-1", "Android") } returns
            Result.Success(RegisterDeviceResponse(id = "device-1", isActive = true))

        manager().registerCurrentToken()

        coVerify { apiService.registerDevice("token-1", "Android") }
        verify { pushStorage.pendingRegistration = false }
    }

    @Test
    fun `keeps token pending when signed out`() = runTest {
        tokenBridge.update("token-1")
        every { tokenStorage.accessToken } returns null

        manager().registerCurrentToken()

        coVerify(exactly = 0) { apiService.registerDevice(any(), any()) }
        verify { pushStorage.pendingRegistration = true }
    }

    @Test
    fun `failed registration stays pending instead of failing the session`() = runTest {
        tokenBridge.update("token-1")
        every { tokenStorage.accessToken } returns "access-token"
        coEvery { apiService.registerDevice("token-1", "Android") } returns
            Result.Error.HttpError(code = 500, message = "Server error")

        manager().registerCurrentToken()

        verify { pushStorage.pendingRegistration = true }
    }

    @Test
    fun `token received is persisted and registered in the background`() = runTest {
        every { tokenStorage.accessToken } returns "access-token"
        coEvery { apiService.registerDevice("token-2", "Android") } returns
            Result.Success(RegisterDeviceResponse(id = "device-1", isActive = true))

        val scope = TestScope(testScheduler)
        val manager = manager(scope)
        manager.onTokenReceived("token-2")
        scope.advanceUntilIdle()

        verify { pushStorage.token = "token-2" }
        coVerify { apiService.registerDevice("token-2", "Android") }
    }

    @Test
    fun `overlapping triggers do not cancel the registration`() = runTest {
        tokenBridge.update("token-1")
        every { tokenStorage.accessToken } returns "access-token"
        coEvery { apiService.registerDevice("token-1", "Android") } returns
            Result.Success(RegisterDeviceResponse(id = "device-1", isActive = true))

        val scope = TestScope(testScheduler)
        val manager = manager(scope)
        manager.onAuthenticated()
        manager.onAuthenticated()
        scope.advanceUntilIdle()

        coVerify(atLeast = 1) { apiService.registerDevice("token-1", "Android") }
    }

    @Test
    fun `unregister deletes the current token`() = runTest {
        tokenBridge.update("token-1")
        every { tokenStorage.accessToken } returns "access-token"
        coEvery { apiService.unregisterDevice("token-1") } returns Result.Success(Unit)

        manager().unregisterCurrentToken()

        coVerify { apiService.unregisterDevice("token-1") }
    }

    @Test
    fun `unregister is skipped without a session`() = runTest {
        tokenBridge.update("token-1")
        every { tokenStorage.accessToken } returns null

        manager().unregisterCurrentToken()

        coVerify(exactly = 0) { apiService.unregisterDevice(any()) }
    }

    private fun manager(scope: TestScope? = null): PushRegistrationManager {
        return if (scope == null) {
            PushRegistrationManager(apiService, tokenStorage, pushStorage, tokenBridge)
        } else {
            PushRegistrationManager(apiService, tokenStorage, pushStorage, tokenBridge, scope)
        }
    }
}
