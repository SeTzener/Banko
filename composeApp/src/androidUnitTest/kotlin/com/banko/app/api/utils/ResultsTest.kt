package com.banko.app.api.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Serializable
data class TestResponse(val message: String, val count: Int)

private fun httpClient(engine: MockEngine): HttpClient {
    return HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json()
        }
    }
}

class ResultsTest {

    @Test
    fun `should return success for 200 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = """{"message":"ok","count":5}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/test")

            assertIs<Result.Success<TestResponse>>(result)
            assertEquals("ok", result.value.message)
            assertEquals(5, result.value.count)
        }
    }

    @Test
    fun `should return success for 201 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = """{"message":"created","count":1}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.Created
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Post, "/create")

            assertIs<Result.Success<TestResponse>>(result)
            assertEquals("created", result.value.message)
        }
    }

    @Test
    fun `should return HttpError for 400 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Bad request",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.BadRequest
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/bad")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(400, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should return HttpError for 401 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Unauthorized",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.Unauthorized
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/unauthorized")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(401, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should return HttpError for 403 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Forbidden",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.Forbidden
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/forbidden")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(403, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should return HttpError for 404 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Not found",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.NotFound
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/notfound")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(404, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should return HttpError for 409 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Conflict",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.Conflict
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/conflict")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(409, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should return HttpError for 500 response`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Server error",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.InternalServerError
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/server-error")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(500, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should return HttpError with body for error response with json body`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = """{"error":"invalid_input","details":"field x is required"}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.UnprocessableEntity
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Post, "/validate")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(422, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should return UnexpectedError when engine throws`() {
        runBlocking {
            val engine = MockEngine { _ ->
                throw java.io.IOException("Connection refused")
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/fail")

            assertTrue(result is Result.Error.UnexpectedError)
        }
    }

    @Test
    fun `should use getSafe convenience method`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Get, request.method)
                assertEquals("/items", request.url.encodedPath)
                respond(
                    content = """{"message":"items","count":0}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = httpClient(engine)

            val result = client.getSafe<TestResponse>("/items")

            assertIs<Result.Success<TestResponse>>(result)
        }
    }

    @Test
    fun `should use postSafe convenience method`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("/create", request.url.encodedPath)
                respond(
                    content = """{"message":"created","count":1}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.Created
                )
            }
            val client = httpClient(engine)

            val result = client.postSafe<TestResponse>("/create")

            assertIs<Result.Success<TestResponse>>(result)
        }
    }

    @Test
    fun `should use putSafe convenience method`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Put, request.method)
                assertEquals("/update", request.url.encodedPath)
                respond(
                    content = """{"message":"updated","count":1}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = httpClient(engine)

            val result = client.putSafe<TestResponse>("/update")

            assertIs<Result.Success<TestResponse>>(result)
        }
    }

    @Test
    fun `should use deleteSafe convenience method`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Delete, request.method)
                assertEquals("/delete/1", request.url.encodedPath)
                respond(
                    content = """{"message":"deleted","count":1}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = httpClient(engine)

            val result = client.deleteSafe<TestResponse>("/delete/1")

            assertIs<Result.Success<TestResponse>>(result)
        }
    }

    @Test
    fun `should send custom headers in request`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("Bearer token123", request.headers["Authorization"])
                assertEquals("application/json", request.headers["Content-Type"])
                respond(
                    content = """{"message":"ok","count":1}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = httpClient(engine)

            val result = client.safeRequest<TestResponse>(HttpMethod.Get, "/secure") {
                header("Authorization", "Bearer token123")
                header("Content-Type", "application/json")
            }

            assertIs<Result.Success<TestResponse>>(result)
        }
    }

    @Test
    fun `should send query parameters in request`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("1", request.url.parameters["page"])
                assertEquals("10", request.url.parameters["limit"])
                respond(
                    content = """{"message":"ok","count":0}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = httpClient(engine)

            val result = client.getSafe<TestResponse>("/items") {
                url.parameters.append("page", "1")
                url.parameters.append("limit", "10")
            }

            assertIs<Result.Success<TestResponse>>(result)
        }
    }
}
