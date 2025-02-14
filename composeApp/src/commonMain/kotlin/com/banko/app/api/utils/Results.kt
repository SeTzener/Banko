package com.banko.app.api.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.util.network.UnresolvedAddressException

sealed interface Result<out D, out E : HttpError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : HttpError>(val error: E) : Result<Nothing, E>
}

suspend inline fun <reified T> HttpClient.getSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Result<T, NetworkError> {
    return try {
        val response = get(url, block)
        when (response.status.value) {
            in 200..299 -> Result.Success(response.body<T>())
            301 -> Result.Error(NetworkError.MOVED_PERMANENTLY)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            402 -> Result.Error(NetworkError.PAYMENT_REQUIRED)
            403 -> Result.Error(NetworkError.FORBIDDEN)
            404 -> Result.Error(NetworkError.NOT_FOUND)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            409 -> Result.Error(NetworkError.CONFLICT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            429 -> Result.Error(NetworkError.TOO_MANY_REQUESTS)
            451 -> Result.Error(NetworkError.UNAVAILABLE_FOR_LEGAL_REASONS)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    } catch (e: Exception) {
        println("Something went wrong in the GET: ${e.message}")
        when (e) {
            is UnresolvedAddressException -> Result.Error(NetworkError.NO_INTERNET)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}

suspend inline fun <reified T> HttpClient.postSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Result<T, NetworkError> {
    return try {
        val response = post(url, block)
        when (response.status.value) {
            in 200..299 -> Result.Success(response.body<T>())
            301 -> Result.Error(NetworkError.MOVED_PERMANENTLY)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            402 -> Result.Error(NetworkError.PAYMENT_REQUIRED)
            403 -> Result.Error(NetworkError.FORBIDDEN)
            404 -> Result.Error(NetworkError.NOT_FOUND)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            409 -> Result.Error(NetworkError.CONFLICT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            429 -> Result.Error(NetworkError.TOO_MANY_REQUESTS)
            451 -> Result.Error(NetworkError.UNAVAILABLE_FOR_LEGAL_REASONS)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Something went wrong in the POST: ${e.message}")
        when (e) {
            is UnresolvedAddressException -> Result.Error(NetworkError.NO_INTERNET)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}

suspend inline fun <reified T> HttpClient.putSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Result<T, NetworkError> {
    return try {
        val response = put(url, block)
        when (response.status.value) {
            in 200..299 -> Result.Success(response.body<T>())
            301 -> Result.Error(NetworkError.MOVED_PERMANENTLY)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            402 -> Result.Error(NetworkError.PAYMENT_REQUIRED)
            403 -> Result.Error(NetworkError.FORBIDDEN)
            404 -> Result.Error(NetworkError.NOT_FOUND)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            409 -> Result.Error(NetworkError.CONFLICT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            429 -> Result.Error(NetworkError.TOO_MANY_REQUESTS)
            451 -> Result.Error(NetworkError.UNAVAILABLE_FOR_LEGAL_REASONS)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Something went wrong in the POST: ${e.message}")
        when (e) {
            is UnresolvedAddressException -> Result.Error(NetworkError.NO_INTERNET)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}

suspend inline fun <reified T> HttpClient.deleteSafe(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {}
): Result<T, NetworkError> {
    return try {
        val response = delete(url, block)
        when (response.status.value) {
            in 200..299 -> Result.Success(response.body<T>())
            301 -> Result.Error(NetworkError.MOVED_PERMANENTLY)
            400 -> Result.Error(NetworkError.BAD_REQUEST)
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            402 -> Result.Error(NetworkError.PAYMENT_REQUIRED)
            403 -> Result.Error(NetworkError.FORBIDDEN)
            404 -> Result.Error(NetworkError.NOT_FOUND)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            409 -> Result.Error(NetworkError.CONFLICT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            429 -> Result.Error(NetworkError.TOO_MANY_REQUESTS)
            451 -> Result.Error(NetworkError.UNAVAILABLE_FOR_LEGAL_REASONS)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    } catch (e: Exception) {
        println("Something went wrong in the GET: ${e.message}")
        when (e) {
            is UnresolvedAddressException -> Result.Error(NetworkError.NO_INTERNET)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}