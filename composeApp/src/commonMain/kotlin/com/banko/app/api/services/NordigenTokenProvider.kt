package com.banko.app.api.services

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.banko.app.api.AccessTokenProvider
import com.banko.app.api.dto.nordigen.Token
import com.banko.app.api.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.util.AttributeKey
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Constants for keys
private val ACCESS_TOKEN_KEY = stringPreferencesKey("nordigenAccessToken")
private val ACCESS_TOKEN_EXPIRES_KEY = longPreferencesKey("nordigenAccessTokenExpires")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("nordigenRefreshToken")
private val REFRESH_TOKEN_EXPIRES_KEY = longPreferencesKey("nordigenRefreshTokenExpires")

/**
 * NordigenTokenProvider is responsible for managing and refreshing access tokens.
 */
class NordigenTokenProvider(
    private val nordigenApi: OauthNordigenApi
) : AccessTokenProvider, KoinComponent {

    private val dataStore: DataStore<Preferences> by inject()
    private var currentTokens: BearerTokens? = null
    private var expiration: TokensExpiration? = null

    override suspend fun getAccessToken(): BearerTokens? {
        getCachedTokens()
        if (currentTokens?.accessToken.isNullOrEmpty() || isTokenExpired(expiration?.accessExpire)) {
            refreshTokens(currentTokens)
        }

        return currentTokens
    }

    private suspend fun getCachedTokens() {
        dataStore.edit { prefs ->
            val accessToken = prefs[ACCESS_TOKEN_KEY] ?: ""
            val accessTokenExpires = prefs[ACCESS_TOKEN_EXPIRES_KEY] ?: 0
            val refreshToken = prefs[REFRESH_TOKEN_KEY] ?: ""
            val refreshTokenExpires = prefs[REFRESH_TOKEN_EXPIRES_KEY] ?: 0

            currentTokens = BearerTokens(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
            expiration = TokensExpiration(
                accessExpire = accessTokenExpires,
                refreshExpire = refreshTokenExpires
            )
        }
    }

    private suspend fun setCache() {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = currentTokens?.accessToken ?: ""
            prefs[ACCESS_TOKEN_EXPIRES_KEY] = expiration?.accessExpire ?: 0

            prefs[REFRESH_TOKEN_KEY] = currentTokens?.refreshToken ?: ""
            prefs[REFRESH_TOKEN_EXPIRES_KEY] = expiration?.refreshExpire ?: 0
        }
    }

    override suspend fun refreshTokens(oldTokens: BearerTokens?): BearerTokens? {
        if (oldTokens?.refreshToken.isNullOrEmpty() || isTokenExpired(expiration?.refreshExpire)) {
            // Fetch new tokens
            val result = nordigenApi.getToken()
            if (result is Result.Error) throw Exception("Failed to fetch new tokens")

            val tokens = (result as Result.Success<Token>).value
            currentTokens = BearerTokens(
                accessToken = tokens.access,
                refreshToken = tokens.refresh
            )
            expiration = TokensExpiration(
                accessExpire = calculateFutureEpoch(tokens.access_expires),
                refreshExpire = calculateFutureEpoch(tokens.refresh_expires)
            )
            setCache()
        } else {
            // Refresh existing tokens
            val result = oldTokens!!.refreshToken?.let { nordigenApi.refreshToken(it) }
            if (result is Result.Error) throw Exception("Failed to refresh tokens")

            val tokens = (result as Result.Success<Token>).value
            currentTokens = BearerTokens(
                accessToken = tokens.access,
                refreshToken = currentTokens?.refreshToken
            )
            expiration = TokensExpiration(
                accessExpire = calculateFutureEpoch(tokens.access_expires),
                refreshExpire = expiration?.refreshExpire ?: 0
            )
            setCache()
        }
        return currentTokens
    }

    private fun isTokenExpired(expirationEpoch: Long?): Boolean {
        val nowEpoch = Clock.System.now().toEpochMilliseconds() / 1000
        return expirationEpoch == null || nowEpoch >= expirationEpoch
    }

    private fun calculateFutureEpoch(expiresIn: Long): Long {
        val nowEpoch = Clock.System.now().toEpochMilliseconds() / 1000
        return nowEpoch + expiresIn - 20 // Subtract a small buffer
    }
}

/**
 * TokenInterceptor handles adding the Authorization header to HTTP requests.
 */
class TokenInterceptor(private val tokenProvider: NordigenTokenProvider) : KoinComponent {

    suspend fun processRequest(requestBuilder: HttpRequestBuilder) {
        val accessToken = tokenProvider.getAccessToken()
        if (accessToken != null) {
            requestBuilder.header("Authorization", "Bearer ${accessToken.accessToken}")
        } else {
            throw Exception("Access token is null")
        }
    }

    class Feature(private val tokenInterceptor: TokenInterceptor) :
        HttpClientPlugin<Unit, Feature>, KoinComponent {

        override val key: AttributeKey<Feature> = AttributeKey("TokenInterceptor")

        override fun prepare(block: Unit.() -> Unit): Feature = this

        override fun install(plugin: Feature, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                plugin.tokenInterceptor.processRequest(context)
                proceed()
            }
        }
    }
}

class TokensExpiration(
    val accessExpire: Long,
    val refreshExpire: Long
)