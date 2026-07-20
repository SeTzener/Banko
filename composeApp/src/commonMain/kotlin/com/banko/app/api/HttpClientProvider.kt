package com.banko.app.api

import com.banko.app.api.utils.jsonAdapters.JsonProvider
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

object HttpClientProvider {
    operator fun invoke(): HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true

        install(ContentNegotiation) {
            json(JsonProvider.json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            connectTimeoutMillis = CONNECT_TIMEOUT
            socketTimeoutMillis = SOCKET_TIMEOUT
            requestTimeoutMillis = REQUEST_TIMEOUT
        }
    }
}

internal const val CONNECT_TIMEOUT: Long = 15_000L
internal const val SOCKET_TIMEOUT: Long = 30_000L
internal const val REQUEST_TIMEOUT: Long = 30_000L
