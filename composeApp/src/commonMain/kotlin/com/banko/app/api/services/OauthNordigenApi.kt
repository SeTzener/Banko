package com.banko.app.api.services

import com.banko.app.api.HttpClientProvider
import com.banko.app.api.dto.nordigen.Token
import com.banko.app.api.utils.NetworkError
import com.banko.app.api.utils.Result
import com.banko.app.api.utils.postSafe
import com.banko.config.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.header.AcceptEncoding
import org.koin.core.component.KoinComponent

class OauthNordigenApi: KoinComponent {
    private val client: HttpClient = HttpClient(HttpClientProvider())

    private val baseUrl = "https://ob.nordigen.com/api"
    private val version = "/v2"

    suspend fun getToken(): Result<Token, NetworkError> {
        return client.postSafe<Token>("$baseUrl$version/token/new/") {
            contentType(ContentType.Application.Json)
            AcceptEncoding("application/json")
            setBody(
                mapOf(
                    "secret_id" to BuildKonfig.NORDIGEN_ID,
                    "secret_key" to BuildKonfig.NORDIGEN_SECRET
                )
            )
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<Token, NetworkError> {
        return client.postSafe<Token>("$baseUrl$version/token/refresh/") {
            contentType(ContentType.Application.Json)
            AcceptEncoding("application/json")
            setBody(
                mapOf(
                    "refresh" to refreshToken
                )
            )
        }
    }
}