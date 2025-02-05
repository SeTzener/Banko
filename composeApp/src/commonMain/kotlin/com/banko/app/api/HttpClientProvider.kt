package com.banko.app.api

import com.banko.app.api.services.TokenInterceptor
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientProvider {
    operator fun invoke(
        tokenInterceptorFeature: TokenInterceptor.Feature? = null,
    ): HttpClientConfig<*>.() -> Unit =  {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            socketTimeoutMillis = TIMEOUT
        }
        tokenInterceptorFeature?.let {
            install(it)
        }
    }
}

internal const val TIMEOUT: Long = 60_000L
