package com.banko.app

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun getEngine(): HttpClientEngineFactory<*> = Darwin
