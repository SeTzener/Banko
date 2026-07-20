package com.banko.app.data.repository

import com.banko.app.api.services.FrankfurterService
import com.banko.app.api.utils.Result
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExchangeRate
import com.banko.app.domain.model.getSupportedCurrencies
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class CurrencyRepository(
    private val database: BankoDatabase,
    private val frankfurterService: FrankfurterService,
) {
    private val exchangeRateDao = database.exchangeRateDao()
    private val supportedCurrencyCodes = getSupportedCurrencies().map { it.code }.toSet()
    private val mapMutex = Mutex()
    private val rateMutexes = mutableMapOf<String, Mutex>()

    fun isCurrencySupported(code: String): Boolean = code in supportedCurrencyCodes

    private suspend fun mutexForKey(vararg parts: String): Mutex = mapMutex.withLock {
        rateMutexes.getOrPut(parts.joinToString("|")) { Mutex() }
    }

    suspend fun getRate(fromCurrency: String, toCurrency: String, date: LocalDate): Double? {
        if (fromCurrency == toCurrency) return 1.0

        return mutexForKey("rate", fromCurrency, toCurrency, date.toString()).withLock {
            val dateStr = date.toString()

            val cached = exchangeRateDao.getRate(fromCurrency, toCurrency, dateStr)
            if (cached != null) return@withLock cached.rate

            val result = frankfurterService.getTimeSeriesRates(fromCurrency, toCurrency, dateStr, dateStr)
            when (result) {
                is Result.Error -> null
                is Result.Success -> {
                    val rate = result.value.rates[dateStr]?.get(toCurrency)
                    exchangeRateDao.upsertExchangeRates(
                        listOf(ExchangeRate(fromCurrency, toCurrency, dateStr, rate))
                    )
                    rate
                }
            }
        }
    }

    suspend fun getRatesForDateRange(
        fromCurrency: String,
        toCurrency: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Map<LocalDate, Double> {
        if (fromCurrency == toCurrency) return emptyMap()

        return mutexForKey("range", fromCurrency, toCurrency).withLock {
            val startStr = startDate.toString()
            val endStr = endDate.toString()

            val cached = exchangeRateDao.getRatesForDateRange(fromCurrency, toCurrency, startStr, endStr)
            val cachedMap = cached.associate { LocalDate.parse(it.date) to it.rate }

            val missingDates = mutableListOf<LocalDate>()
            var current = startDate
            while (current <= endDate) {
                if (current !in cachedMap) {
                    missingDates.add(current)
                }
                current = current.plus(DatePeriod(days = 1))
            }

            if (missingDates.isEmpty()) {
                val filtered = cachedMap.filterValues { it != null }.mapValues { it.value!! }
                return@withLock fillGaps(filtered, startDate, endDate)
            }

            val actualStart = missingDates.first()
            val actualEnd = missingDates.last()

            val result = frankfurterService.getTimeSeriesRates(fromCurrency, toCurrency, actualStart.toString(), actualEnd.toString())
            when (result) {
                is Result.Error -> {
                    val filtered = cachedMap.filterValues { it != null }.mapValues { it.value!! }
                    fillGaps(filtered, startDate, endDate)
                }
                is Result.Success -> {
                    val ratesToInsert = mutableListOf<ExchangeRate>()
                    var apiCurrent = actualStart
                    while (apiCurrent <= actualEnd) {
                        val dateStr = apiCurrent.toString()
                        val rate = result.value.rates[dateStr]?.get(toCurrency)
                        ratesToInsert.add(ExchangeRate(fromCurrency, toCurrency, dateStr, rate))
                        apiCurrent = apiCurrent.plus(DatePeriod(days = 1))
                    }
                    exchangeRateDao.upsertExchangeRates(ratesToInsert)
                    val fetchedMap = ratesToInsert.associate { LocalDate.parse(it.date) to it.rate }
                    val merged = (cachedMap + fetchedMap).filterValues { it != null }.mapValues { it.value!! }
                    fillGaps(merged, startDate, endDate)
                }
            }
        }
    }

    private fun fillGaps(
        rates: Map<LocalDate, Double>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Map<LocalDate, Double> {
        val result = mutableMapOf<LocalDate, Double>()
        var lastRate: Double? = null
        var current = startDate
        while (current <= endDate) {
            val rate = rates[current]
            if (rate != null) {
                lastRate = rate
            }
            if (lastRate != null) {
                result[current] = lastRate
            }
            current = current.plus(DatePeriod(days = 1))
        }
        return result
    }

    suspend fun getRateCount(): Long = exchangeRateDao.getRateCount()
}
