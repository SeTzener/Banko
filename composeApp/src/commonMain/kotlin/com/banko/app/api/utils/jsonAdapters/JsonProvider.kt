package com.banko.app.api.utils.jsonAdapters

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

object JsonProvider {
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(ColorSerializer) // Register Color serializer globally
        }
    }
}