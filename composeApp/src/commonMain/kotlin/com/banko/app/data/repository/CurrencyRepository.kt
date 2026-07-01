package com.banko.app.data.repository

import com.banko.app.api.services.FrankfurterService
import com.banko.app.api.utils.Result
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExchangeRate
import com.banko.app.domain.model.getSupportedCurrencies
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class CurrencyRepository(
    private val database: BankoDatabase,
    private val frankfurterService: FrankfurterService,
) {
    private val exchangeRateDao = database.exchangeRateDao()
    private val supportedCurrencyCodes = getSupportedCurrencies().map { it.code }.toSet()

    fun isCurrencySupported(code: String): Boolean = code in supportedCurrencyCodes

    suspend fun getRate(fromCurrency: String, toCurrency: String, date: LocalDate): Double? {
        if (fromCurrency == toCurrency) return 1.0

        val dateStr = date.toString()

        val cached = exchangeRateDao.getRate(fromCurrency, toCurrency, dateStr)
        if (cached != null) return cached

        val result = frankfurterService.getTimeSeriesRates(fromCurrency, toCurrency, dateStr, dateStr)
        return when (result) {
            is Result.Error -> null
            is Result.Success -> {
                val rate = result.value.rates[dateStr]?.get(toCurrency)
                if (rate != null) {
                    exchangeRateDao.upsertExchangeRates(
                        listOf(ExchangeRate(fromCurrency, toCurrency, dateStr, rate))
                    )
                }
                rate
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

        if (missingDates.isEmpty()) return cachedMap

        val actualStart = missingDates.first()
        val actualEnd = missingDates.last()

        val result = frankfurterService.getTimeSeriesRates(fromCurrency, toCurrency, actualStart.toString(), actualEnd.toString())
        when (result) {
            is Result.Error -> return cachedMap
            is Result.Success -> {
                val ratesToInsert = mutableListOf<ExchangeRate>()
                result.value.rates.forEach { (dateStr, ratesMap) ->
                    val rate = ratesMap[toCurrency]
                    if (rate != null) {
                        ratesToInsert.add(ExchangeRate(fromCurrency, toCurrency, dateStr, rate))
                    }
                }
                if (ratesToInsert.isNotEmpty()) {
                    exchangeRateDao.upsertExchangeRates(ratesToInsert)
                }
                val fetchedMap = ratesToInsert.associate { LocalDate.parse(it.date) to it.rate }
                return cachedMap + fetchedMap
            }
        }
    }

    suspend fun getRateCount(): Long = exchangeRateDao.getRateCount()
}
