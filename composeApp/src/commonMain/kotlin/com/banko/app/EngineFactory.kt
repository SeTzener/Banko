package com.banko.app

import io.ktor.client.engine.HttpClientEngineFactory

expect fun getEngine(): HttpClientEngineFactory<*>