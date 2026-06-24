package com.banko.app.api.services

import com.banko.app.api.dto.bankoApi.AuthResponse
import com.banko.app.api.dto.bankoApi.ExpenseTag
import com.banko.app.api.dto.bankoApi.ExpenseTags
import com.banko.app.api.dto.bankoApi.LoginRequest
import com.banko.app.api.dto.bankoApi.RefreshRequest
import com.banko.app.api.dto.bankoApi.RegisterRequest
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.dto.bankoApi.UpsertExpenseTag
import com.banko.app.api.dto.bankoApi.UserExportData
import com.banko.app.api.dto.bankoApi.UserProfileResponse
import com.banko.app.api.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private fun httpClient(engine: MockEngine): HttpClient {
    return HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
}

class BankoApiServiceTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should get transactions with page params`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/transactions/", request.url.encodedPath)
                assertEquals("1", request.url.parameters["pageNumber"])
                assertEquals("50", request.url.parameters["pageSize"])
                assertEquals(HttpMethod.Get, request.method)
                respond(
                    content = """{"transactions":[],"totalCount":0,"pageNumber":1,"pageSize":50}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.getTransactions(1, 50)

            assertIs<Result.Success<Transactions>>(result)
            assertEquals(0, result.value.totalCount)
            assertTrue(result.value.transactions.isEmpty())
        }
    }

    @Test
    fun `should get transactions with date range`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/transactions/", request.url.encodedPath)
                assertEquals("1", request.url.parameters["pageNumber"])
                assertEquals("10", request.url.parameters["pageSize"])
                assertEquals("2024-01-01", request.url.parameters["fromDate"])
                assertEquals("2024-01-31", request.url.parameters["toDate"])
                respond(
                    content = """{"transactions":[],"totalCount":0,"pageNumber":1,"pageSize":10}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.getTransactions(
                1, 10,
                fromDate = LocalDate(2024, 1, 1),
                toDate = LocalDate(2024, 1, 31)
            )

            assertIs<Result.Success<Transactions>>(result)
        }
    }

    @Test
    fun `should return error when getTransactions fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Not found",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.NotFound
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.getTransactions(1, 50)

            assertTrue(result is Result.Error.HttpError)
            assertEquals(404, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should get expense tags`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/settings/expense-tags", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                respond(
                    content = """{"expenseTags":[{"id":"tag-1","name":"Food","color":16711680,"isEarning":false,"aka":[]}]}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.getExpenseTags()

            assertIs<Result.Success<ExpenseTags>>(result)
            assertEquals(1, result.value.expenseTags.size)
            assertEquals("Food", result.value.expenseTags[0].name)
        }
    }

    @Test
    fun `should return error when getExpenseTags fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Unauthorized",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.Unauthorized
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.getExpenseTags()

            assertTrue(result is Result.Error.HttpError)
            assertEquals(401, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should update expense tag`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Put, request.method)
                assertEquals("/settings/expense-tag/tag-1", request.url.encodedPath)
                respond(
                    content = """{"expenseTag":{"id":"tag-1","name":"Updated","color":16711680,"isEarning":false,"aka":[]}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))
            val tag = ExpenseTag(id = "tag-1", name = "Updated", color = 16711680, isEarning = false, aka = emptyList())

            val result = service.updateExpenseTag(tag)

            assertIs<Result.Success<UpsertExpenseTag>>(result)
            assertEquals("Updated", result.value.expenseTag.name)
        }
    }

    @Test
    fun `should return error when updateExpenseTag fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Forbidden",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.Forbidden
                )
            }
            val service = BankoApiService(httpClient(engine))
            val tag = ExpenseTag(id = "tag-1", name = "Test", color = 0, isEarning = false, aka = null)

            val result = service.updateExpenseTag(tag)

            assertTrue(result is Result.Error.HttpError)
            assertEquals(403, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should create expense tag`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("/settings/expense-tag", request.url.encodedPath)
                respond(
                    content = """{"expenseTag":{"id":"new-id","name":"NewTag","color":255,"isEarning":true,"aka":null}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.Created
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.createExpenseTag("NewTag", 255, true)

            assertIs<Result.Success<UpsertExpenseTag>>(result)
            assertEquals("NewTag", result.value.expenseTag.name)
            assertEquals(true, result.value.expenseTag.isEarning)
        }
    }

    @Test
    fun `should return error when createExpenseTag fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Conflict",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.Conflict
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.createExpenseTag("Dup", 0, false)

            assertTrue(result is Result.Error.HttpError)
            assertEquals(409, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should delete expense tag`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Delete, request.method)
                assertEquals("/settings/expense-tag/tag-1", request.url.encodedPath)
                respond(
                    content = "",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.deleteExpenseTag("tag-1")

            assertIs<Result.Success<Unit>>(result)
        }
    }

    @Test
    fun `should return error when deleteExpenseTag fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Not found",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.NotFound
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.deleteExpenseTag("tag-1")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(404, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should assign expense tag to transaction`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Put, request.method)
                assertEquals("/transactions/expense-tag", request.url.encodedPath)
                respond(
                    content = "",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.assignExpenseTag("tx-1", "tag-2")

            assertIs<Result.Success<Unit>>(result)
        }
    }

    @Test
    fun `should assign expense tag to transaction with null tag`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/transactions/expense-tag", request.url.encodedPath)
                respond(
                    content = "",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.assignExpenseTag("tx-1", null)

            assertIs<Result.Success<Unit>>(result)
        }
    }

    @Test
    fun `should save note`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Put, request.method)
                assertEquals("/transactions/tx-1/note", request.url.encodedPath)
                respond(
                    content = """{"note":"my note"}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.saveNote("tx-1", "my note")

            assertIs<Result.Success<String>>(result)
        }
    }

    @Test
    fun `should return error when saveNote fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Server error",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.InternalServerError
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.saveNote("tx-1", "test")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(500, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should delete transaction`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals(HttpMethod.Delete, request.method)
                assertEquals("/transactions/tx-1", request.url.encodedPath)
                respond(
                    content = """{"deleted":"tx-1"}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.deleteTransaction("tx-1")

            assertIs<Result.Success<String>>(result)
        }
    }

    @Test
    fun `should return error when deleteTransaction fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Not found",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    status = HttpStatusCode.NotFound
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.deleteTransaction("tx-1")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(404, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should login with email and password`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/login", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                respond(
                    content = """{"accountId":"acc-1","accessToken":"tok","refreshToken":"ref","expiresIn":900}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.login("user@test.com", "password")

            assertIs<Result.Success<AuthResponse>>(result)
            assertEquals("acc-1", result.value.accountId)
            assertEquals("tok", result.value.accessToken)
        }
    }

    @Test
    fun `should return error when login fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = """{"message":"WrongCredentials"}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.Unauthorized
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.login("bad@test.com", "wrong")

            assertTrue(result is Result.Error.HttpError)
            assertEquals(401, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should register new user`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                respond(
                    content = """{"accountId":"acc-2","accessToken":"tok2","refreshToken":"ref2","expiresIn":900}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.register("new@test.com", "password12345", "New User", true)

            assertIs<Result.Success<AuthResponse>>(result)
            assertEquals("acc-2", result.value.accountId)
        }
    }

    @Test
    fun `should refresh token`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/refresh", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                respond(
                    content = """{"accountId":"acc-1","accessToken":"new-tok","refreshToken":"new-ref","expiresIn":900}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.refreshToken("old-refresh")

            assertIs<Result.Success<AuthResponse>>(result)
            assertEquals("new-tok", result.value.accessToken)
        }
    }

    @Test
    fun `should get profile`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/me", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                respond(
                    content = """{"accountId":"acc-1","email":"user@test.com","fullName":"Test","consentGiven":true,"createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.getProfile()

            assertIs<Result.Success<UserProfileResponse>>(result)
            assertEquals("user@test.com", result.value.email)
            assertEquals("Test", result.value.fullName)
        }
    }

    @Test
    fun `should return error when getProfile fails`() {
        runBlocking {
            val engine = MockEngine { _ ->
                respond(
                    content = "Not found",
                    status = HttpStatusCode.NotFound
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.getProfile()

            assertTrue(result is Result.Error.HttpError)
            assertEquals(404, (result as Result.Error.HttpError).code)
        }
    }

    @Test
    fun `should update profile`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/me", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                respond(
                    content = """{"accountId":"acc-1","email":"user@test.com","fullName":"Updated","consentGiven":true,"createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-02T00:00:00Z"}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.updateProfile(com.banko.app.api.dto.bankoApi.UpdateProfileRequest(fullName = "Updated"))

            assertIs<Result.Success<UserProfileResponse>>(result)
            assertEquals("Updated", result.value.fullName)
        }
    }

    @Test
    fun `should change password`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/me/password", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                respond(
                    content = "",
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.changePassword("old", "newpassword12345")

            assertIs<Result.Success<Unit>>(result)
        }
    }

    @Test
    fun `should accept consent`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/me/consent", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                respond(
                    content = "",
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.acceptConsent("policy-1")

            assertIs<Result.Success<Unit>>(result)
        }
    }

    @Test
    fun `should export data`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/me/export", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                respond(
                    content = """{"accountId":"acc-1","email":"user@test.com","consentGiven":true,"createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z","consentLogs":[{"policyVersion":"1.0","policyTitle":"Policy v1","accepted":true,"recordedAt":"2024-01-01T00:00:00Z"}]}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.exportData()

            assertIs<Result.Success<UserExportData>>(result)
            assertEquals(1, result.value.consentLogs.size)
        }
    }

    @Test
    fun `should delete account`() {
        runBlocking {
            val engine = MockEngine { request ->
                assertEquals("/Users/me", request.url.encodedPath)
                assertEquals(HttpMethod.Delete, request.method)
                respond(
                    content = "",
                    status = HttpStatusCode.OK
                )
            }
            val service = BankoApiService(httpClient(engine))

            val result = service.deleteAccount()

            assertIs<Result.Success<Unit>>(result)
        }
    }
}
