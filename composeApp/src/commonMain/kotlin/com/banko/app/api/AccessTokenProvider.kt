package com.banko.app.api

import io.ktor.client.plugins.auth.providers.BearerTokens

interface AccessTokenProvider {
    suspend fun getAccessToken(): BearerTokens?
    suspend fun refreshTokens(oldTokens: BearerTokens?): BearerTokens?
}