package com.banko.app.api.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.http.HttpMethod
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

sealed interface Result<out T> {
    data class Success<out T>(val value: T) : Result<T>
    sealed interface Error : Result<Nothing> {
        data class HttpError(
            val code: Int,
            val message: String,
            val description: String? = null,
            val body: String? = null
        ) : Error {
            fun errorMessageToDisplay(): String {
                return "HttpError: $code $description"
            }

            fun fullErrorMessage(): String {
                return "HttpError(code=$code, message='$message', body='$body')"
            }
        }

        data class NetworkError(val exception: Throwable) : Error
        data class SerializationError(val exception: Throwable) : Error
        data class UnexpectedError(val exception: Throwable) : Error
    }
}

suspend inline fun <reified T> HttpClient.safeRequest(
    method: HttpMethod,
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Result<T> {
    return try {
        val response = request(url) {
            this.method = method
            block()
        }

        Result.Success(response.body<T>())
    } catch (e: RedirectResponseException) {
        // 3xx responses
        Result.Error.HttpError(
            code = e.response.status.value,
            description = e.response.status.description,
            message = "Redirect error: ${e.message}",
            body = try { e.response.body() } catch (e: Exception) { null }
        )
    } catch (e: ClientRequestException) {
        // 4xx responses
        Result.Error.HttpError(
            code = e.response.status.value,
            description = e.response.status.description,
            message = "Client error: ${e.message}",
            body = try { e.response.body() } catch (e: Exception) { null }
        )
    } catch (e: ServerResponseException) {
        // 5xx responses
        Result.Error.HttpError(
            code = e.response.status.value,
            description = e.response.status.description,
            message = "Server error: ${e.message}",
            body = try { e.response.body() } catch (e: Exception) { null }
        )
    } catch (e: UnresolvedAddressException) {
        Result.Error.NetworkError(e)
    } catch (e: SerializationException) {
        Result.Error.SerializationError(e)
    } catch (e: Exception) {
        Result.Error.UnexpectedError(e)
    }
}

// Convenience extensions for specific HTTP methods
suspend inline fun <reified T> HttpClient.getSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
) = safeRequest<T>(HttpMethod.Get, url, block)

suspend inline fun <reified T> HttpClient.postSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
) = safeRequest<T>(HttpMethod.Post, url, block)

suspend inline fun <reified T> HttpClient.putSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
) = safeRequest<T>(HttpMethod.Put, url, block)

suspend inline fun <reified T> HttpClient.deleteSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
) = safeRequest<T>(HttpMethod.Delete, url, block)