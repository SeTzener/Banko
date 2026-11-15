package com.banko.app.push

import com.banko.app.api.auth.TokenStorage
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Registers the current FCM token with the backend.
 *
 * Push is a best-effort side channel: failures are persisted as pending and
 * retried on the next trigger (app launch, token refresh, sign-in). They must
 * never log the user out or surface an auth error.
 */
class PushRegistrationManager(
    private val apiService: BankoApiService,
    private val tokenStorage: TokenStorage,
    private val pushStorage: PushStorage,
    private val tokenBridge: PushTokenBridge,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val mutex = Mutex()
    private var registrationJob: Job? = null
    private var rerunRequested = false

    init {
        // A token received before Koin was ready (iOS launch) is picked up here.
        // Never overwrite a token that already arrived through the bridge.
        if (tokenBridge.current() == null) {
            pushStorage.token?.let(tokenBridge::update)
        }
    }

    fun onTokenReceived(token: String) {
        if (token.isBlank()) return
        pushStorage.token = token
        tokenBridge.update(token)
        registerInBackground()
    }

    fun onAuthenticated() {
        registerInBackground()
    }

    fun registerInBackground() {
        // Never cancel an in-flight registration: overlapping triggers are
        // normal (session restore, token refresh, FCM token delivery) and a
        // cancelled request would be recorded as a failure. Instead, ask the
        // running job to register again so the newest token wins.
        if (registrationJob?.isActive == true) {
            rerunRequested = true
            return
        }

        registrationJob = scope.launch {
            do {
                rerunRequested = false
                registerCurrentToken()
            } while (rerunRequested)
        }
    }

    suspend fun registerCurrentToken() {
        val token = tokenBridge.current() ?: pushStorage.token ?: return

        if (tokenStorage.accessToken == null) {
            // Signed out: keep the token so the next sign-in can register it.
            pushStorage.pendingRegistration = true
            return
        }

        mutex.withLock {
            when (apiService.registerDevice(token, pushPlatformName)) {
                is Result.Success -> pushStorage.pendingRegistration = false
                is Result.Error -> pushStorage.pendingRegistration = true
            }
        }
    }

    /**
     * Called before the auth session is cleared. The caller bounds this call so
     * an offline logout cannot hang.
     */
    suspend fun unregisterCurrentToken() {
        val token = tokenBridge.current() ?: pushStorage.token ?: return
        if (tokenStorage.accessToken == null) return

        mutex.withLock {
            apiService.unregisterDevice(token)
            pushStorage.pendingRegistration = false
        }
    }
}
