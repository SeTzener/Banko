package com.banko.app.database.Entities

import androidx.room.Entity

@Entity(
    tableName = "exchange_rates",
    primaryKeys = ["fromCurrency", "toCurrency", "date"]
)
data class ExchangeRate(
    val fromCurrency: String,
    val toCurrency: String,
    val date: String,
    val rate: Double,
)
