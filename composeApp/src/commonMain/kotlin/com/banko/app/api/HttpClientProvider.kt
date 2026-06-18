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
            socketTimeoutMillis = TIMEOUT
        }
    }
}

internal const val TIMEOUT: Long = 60_000L
