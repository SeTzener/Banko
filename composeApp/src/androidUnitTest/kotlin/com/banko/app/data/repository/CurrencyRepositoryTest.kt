package com.banko.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.api.services.FrankfurterService
import com.banko.app.database.BankoDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CurrencyRepositoryTest {

    private lateinit var db: BankoDatabase
    private lateinit var repository: CurrencyRepository
    private var lastEngine: MockEngine? = null

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun repositoryWithResponse(json: String, status: HttpStatusCode = HttpStatusCode.OK): CurrencyRepository {
        val engine = MockEngine { _ ->
            respond(
                content = json,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = status
            )
        }
        lastEngine = engine
        val client = HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val service = FrankfurterService(client)
        return CurrencyRepository(db, service)
    }

    @Test
    fun `getRate should return 1_0 when currencies are the same`() {
        runBlocking {
            val repo = repositoryWithResponse("{}")
            val rate = repo.getRate("NOK", "NOK", LocalDate(2026, 1, 15))
            assertEquals(1.0, rate)
        }
    }

    @Test
    fun `getRate should return cached value on second call`() {
        runBlocking {
            var callCount = 0
            val engine = MockEngine { _ ->
                callCount++
                respond(
                    content = """
                {
                    "amount": 1.0,
                    "base": "NOK",
                    "date": "2026-01-15",
                    "rates": {"2026-01-15": {"EUR": 0.085}}
                }
                """.trimIndent(),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val repo = CurrencyRepository(db, FrankfurterService(client))

            val first = repo.getRate("NOK", "EUR", LocalDate(2026, 1, 15))
            val second = repo.getRate("NOK", "EUR", LocalDate(2026, 1, 15))

            assertEquals(0.085, first!!, 0.0001)
            assertEquals(0.085, second!!, 0.0001)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `getRate should return null when API returns no rate for that date`() {
        runBlocking {
            val repo = repositoryWithResponse("""
        {
            "amount": 1.0,
            "base": "NOK",
            "date": "2026-01-10",
            "rates": {}
        }
        """.trimIndent())

            val rate = repo.getRate("NOK", "EUR", LocalDate(2026, 1, 10))

            assertNull(rate)
        }
    }

    @Test
    fun `getRate should not call API again after null rate is cached`() {
        runBlocking {
            var callCount = 0
            val engine = MockEngine { _ ->
                callCount++
                respond(
                    content = """{"amount":1.0,"base":"NOK","date":"2026-01-10","rates":{}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val repo = CurrencyRepository(db, FrankfurterService(client))

            val first = repo.getRate("NOK", "EUR", LocalDate(2026, 1, 10))
            val second = repo.getRate("NOK", "EUR", LocalDate(2026, 1, 10))

            assertNull(first)
            assertNull(second)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `getRate should return null on API error`() {
        runBlocking {
            val repo = repositoryWithResponse("Error", HttpStatusCode.InternalServerError)

            val rate = repo.getRate("NOK", "EUR", LocalDate(2026, 1, 15))

            assertNull(rate)
        }
    }

    @Test
    fun `getRatesForDateRange should return empty map when currencies are the same`() {
        runBlocking {
            val repo = repositoryWithResponse("{}")
            val rates = repo.getRatesForDateRange("NOK", "NOK", LocalDate(2026, 1, 1), LocalDate(2026, 1, 31))
            assertTrue(rates.isEmpty())
        }
    }

    @Test
    fun `getRatesForDateRange should return cached rates without API call`() {
        runBlocking {
            var callCount = 0
            val engine = MockEngine { _ ->
                callCount++
                respond(
                    content = """
                {
                    "amount": 1.0,
                    "base": "NOK",
                    "start_date": "2026-01-01",
                    "end_date": "2026-01-03",
                    "rates": {
                        "2026-01-01": {"EUR": 0.085},
                        "2026-01-02": {"EUR": 0.086},
                        "2026-01-03": {"EUR": 0.084}
                    }
                }
                """.trimIndent(),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val repo = CurrencyRepository(db, FrankfurterService(client))

            val first = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 3))
            val second = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 3))

            assertEquals(3, first.size)
            assertEquals(3, second.size)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `getRatesForDateRange should not re-fetch when weekends are in cache with null`() {
        runBlocking {
            var callCount = 0
            val engine = MockEngine { _ ->
                callCount++
                respond(
                    content = """
                {
                    "amount": 1.0,
                    "base": "NOK",
                    "start_date": "2026-01-01",
                    "end_date": "2026-01-04",
                    "rates": {
                        "2026-01-01": {"EUR": 0.085},
                        "2026-01-02": {"EUR": 0.086}
                    }
                }
                """.trimIndent(),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val repo = CurrencyRepository(db, FrankfurterService(client))

            val first = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 4))

            val second = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 4))

            assertEquals(4, first.size)
            assertEquals(4, second.size)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `getRatesForDateRange should fetch only missing sub-range`() {
        runBlocking {
            var callCount = 0
            val engine = MockEngine { _ ->
                callCount++
                respond(
                    content = """
                {
                    "amount": 1.0,
                    "base": "NOK",
                    "start_date": "2026-01-01",
                    "end_date": "2026-01-03",
                    "rates": {
                        "2026-01-01": {"EUR": 0.085},
                        "2026-01-02": {"EUR": 0.086},
                        "2026-01-03": {"EUR": 0.084}
                    }
                }
                """.trimIndent(),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    status = HttpStatusCode.OK
                )
            }
            val client = HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val repo = CurrencyRepository(db, FrankfurterService(client))

            val first = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 3))
            assertEquals(3, first.size)
            assertEquals(1, callCount)

            val wide = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 5))
            assertEquals(2, callCount)

            assertEquals(5, wide.size)
            // Null-rate entries are filtered from the returned map
            assertEquals(0.085, wide[LocalDate(2026, 1, 1)])
            assertEquals(0.084, wide[LocalDate(2026, 1, 3)])
        }
    }

    @Test
    fun `getRatesForDateRange should return partial results on API error`() {
        runBlocking {
            var callCount = 0
            val engine = MockEngine { _ ->
                callCount++
                if (callCount == 1) {
                    respond(
                        content = """
                    {
                        "amount": 1.0,
                        "base": "NOK",
                        "start_date": "2026-01-01",
                        "end_date": "2026-01-03",
                        "rates": {
                            "2026-01-01": {"EUR": 0.085},
                            "2026-01-02": {"EUR": 0.086},
                            "2026-01-03": {"EUR": 0.084}
                        }
                    }
                    """.trimIndent(),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        status = HttpStatusCode.OK
                    )
                } else {
                    respond(
                        content = "Server error",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            val client = HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val repo = CurrencyRepository(db, FrankfurterService(client))

            val first = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 3))
            assertEquals(3, first.size)

            val extended = repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 5))

            assertEquals(5, extended.size)
        }
    }

    @Test
    fun `getRateCount should return number of stored rates`() {
        runBlocking {
            val repo = repositoryWithResponse("""
        {
            "amount": 1.0,
            "base": "NOK",
            "start_date": "2026-01-01",
            "end_date": "2026-01-02",
            "rates": {
                "2026-01-01": {"EUR": 0.085},
                "2026-01-02": {"EUR": 0.086}
            }
        }
        """.trimIndent())

            assertEquals(0, repo.getRateCount())

            repo.getRatesForDateRange("NOK", "EUR", LocalDate(2026, 1, 1), LocalDate(2026, 1, 2))

            val count = repo.getRateCount()
            assertTrue(count >= 2)
        }
    }

    @Test
    fun `isCurrencySupported should return true for known currencies`() {
        runBlocking {
            val repo = repositoryWithResponse("{}")
            assertTrue(repo.isCurrencySupported("EUR"))
            assertTrue(repo.isCurrencySupported("NOK"))
            assertTrue(repo.isCurrencySupported("USD"))
        }
    }

    @Test
    fun `isCurrencySupported should return false for unknown currencies`() {
        runBlocking {
            val repo = repositoryWithResponse("{}")
            assertTrue(!repo.isCurrencySupported("XYZ"))
        }
    }
}
