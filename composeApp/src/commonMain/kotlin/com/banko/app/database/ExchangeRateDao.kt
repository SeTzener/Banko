package com.banko.app.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.banko.app.database.Entities.ExchangeRate
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Upsert
    suspend fun upsertExchangeRates(rates: List<ExchangeRate>)

    @Query(
        """
        SELECT rate FROM exchange_rates 
        WHERE fromCurrency = :fromCurrency 
        AND toCurrency = :toCurrency 
        AND date = :date 
        LIMIT 1
        """
    )
    suspend fun getRate(fromCurrency: String, toCurrency: String, date: String): Double?

    @Query(
        """
        SELECT * FROM exchange_rates 
        WHERE fromCurrency = :fromCurrency 
        AND toCurrency = :toCurrency 
        AND date >= :startDate 
        AND date <= :endDate
        """
    )
    suspend fun getRatesForDateRange(
        fromCurrency: String,
        toCurrency: String,
        startDate: String,
        endDate: String,
    ): List<ExchangeRate>

    @Query("SELECT COUNT(*) FROM exchange_rates")
    suspend fun getRateCount(): Long
}
