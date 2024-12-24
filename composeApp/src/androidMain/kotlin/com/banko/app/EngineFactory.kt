package com.banko.app

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun getEngine(): HttpClientEngineFactory<*> = OkHttp
