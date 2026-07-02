package com.banko.app.database.entities

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExchangeRate
import com.banko.app.database.ExchangeRateDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExchangeRateDaoTest {

    private lateinit var db: BankoDatabase
    private lateinit var dao: ExchangeRateDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.exchangeRateDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should upsert and query single rate by date`() = runBlocking {
        val rate = ExchangeRate(fromCurrency = "NOK", toCurrency = "EUR", date = "2026-01-15", rate = 0.085)

        dao.upsertExchangeRates(listOf(rate))

        val loaded = dao.getRate("NOK", "EUR", "2026-01-15")
        assertEquals(0.085, loaded!!.rate!!, 0.0001)
    }

    @Test
    fun `should return null when querying non-existent rate`() = runBlocking {
        val loaded = dao.getRate("NOK", "EUR", "2026-06-01")
        assertNull(loaded)
    }

    @Test
    fun `should upsert multiple rates and query date range`() = runBlocking {
        val rates = listOf(
            ExchangeRate("NOK", "EUR", "2026-01-01", 0.085),
            ExchangeRate("NOK", "EUR", "2026-01-02", 0.086),
            ExchangeRate("NOK", "EUR", "2026-01-03", 0.084),
        )

        dao.upsertExchangeRates(rates)

        val loaded = dao.getRatesForDateRange("NOK", "EUR", "2026-01-01", "2026-01-03")
        assertEquals(3, loaded.size)
    }

    @Test
    fun `getRatesForDateRange should filter by currency pair`() = runBlocking {
        val rates = listOf(
            ExchangeRate("NOK", "EUR", "2026-01-01", 0.085),
            ExchangeRate("USD", "EUR", "2026-01-01", 0.92),
        )

        dao.upsertExchangeRates(rates)

        val loaded = dao.getRatesForDateRange("NOK", "EUR", "2026-01-01", "2026-01-01")
        assertEquals(1, loaded.size)
        assertEquals("NOK", loaded[0].fromCurrency)
    }

    @Test
    fun `getRatesForDateRange should return empty list for no matches`() = runBlocking {
        val loaded = dao.getRatesForDateRange("NOK", "EUR", "2026-01-01", "2026-01-10")
        assertEquals(0, loaded.size)
    }

    @Test
    fun `getRatesForDateRange should respect date boundaries`() = runBlocking {
        val rates = listOf(
            ExchangeRate("NOK", "EUR", "2026-01-01", 0.085),
            ExchangeRate("NOK", "EUR", "2026-01-15", 0.086),
            ExchangeRate("NOK", "EUR", "2026-02-01", 0.087),
        )

        dao.upsertExchangeRates(rates)

        val loaded = dao.getRatesForDateRange("NOK", "EUR", "2026-01-01", "2026-01-31")
        assertEquals(2, loaded.size)
    }

    @Test
    fun `should upsert and update existing rate`() = runBlocking {
        val rate = ExchangeRate("NOK", "EUR", "2026-01-15", 0.085)

        dao.upsertExchangeRates(listOf(rate))
        val updated = ExchangeRate("NOK", "EUR", "2026-01-15", 0.090)
        dao.upsertExchangeRates(listOf(updated))

        val loaded = dao.getRate("NOK", "EUR", "2026-01-15")
        assertEquals(0.090, loaded!!.rate!!, 0.0001)
    }

    @Test
    fun `should store null rate and return null`() = runBlocking {
        val rate = ExchangeRate("NOK", "EUR", "2026-01-15", null)

        dao.upsertExchangeRates(listOf(rate))

        val loaded = dao.getRate("NOK", "EUR", "2026-01-15")
        assertNotNull(loaded)
        assertNull(loaded.rate)
    }

    @Test
    fun `getRatesForDateRange should include null-rate entries`() = runBlocking {
        val rates = listOf(
            ExchangeRate("NOK", "EUR", "2026-01-15", 0.085),
            ExchangeRate("NOK", "EUR", "2026-01-16", null),
        )

        dao.upsertExchangeRates(rates)

        val loaded = dao.getRatesForDateRange("NOK", "EUR", "2026-01-15", "2026-01-16")
        assertEquals(2, loaded.size)
        val rate = loaded.find { it.date == "2026-01-16" }
        assertNotNull(rate)
        assertNull(rate.rate)
    }

    @Test
    fun `getRateCount should return correct count`() = runBlocking {
        assertEquals(0, dao.getRateCount())

        dao.upsertExchangeRates(listOf(
            ExchangeRate("NOK", "EUR", "2026-01-01", 0.085),
            ExchangeRate("NOK", "EUR", "2026-01-02", 0.086),
        ))

        assertEquals(2, dao.getRateCount())
    }

    @Test
    fun `should handle different fromCurrencies independently`() = runBlocking {
        dao.upsertExchangeRates(listOf(
            ExchangeRate("NOK", "EUR", "2026-01-01", 0.085),
            ExchangeRate("SEK", "EUR", "2026-01-01", 0.088),
        ))

        val nokRate = dao.getRate("NOK", "EUR", "2026-01-01")
        val sekRate = dao.getRate("SEK", "EUR", "2026-01-01")

        assertEquals(0.085, nokRate!!.rate!!, 0.0001)
        assertEquals(0.088, sekRate!!.rate!!, 0.0001)
    }
}
