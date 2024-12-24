package com.banko.app.api.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

// TODO(): temporary, change it with the actual service
class NordigenApiService(private val client: HttpClient) {
    suspend fun getExampleData(): ExampleData {
        return client.get("https://catfact.ninja/fact").body()
    }
}

@Serializable
data class ExampleData(
    val fact: String
)