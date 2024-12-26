package com.banko.app.ui.models

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

class Transaction (
    val id: Int,
    val remittanceInfo: String,
    val date: LocalDate,
    val amount: Float,
    val currency: String,
    val category: Category
)

fun createMockedTransaction(): List<Transaction> {
    val result = mutableListOf<Transaction>()
    var currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    for (i in 1..100){
        result.add(
            Transaction(
                id = i,
                remittanceInfo = "Remittance $i",
                date = currentDateTime.date,
                amount = Random.nextFloat() * 1000, // Random amount
                currency = "Nok",
                category = categories.random()
            )
        )
        val randomMinutes = Random.nextInt(0, 1440)
        currentDateTime = currentDateTime.toInstant(TimeZone.currentSystemDefault()).minus(randomMinutes, DateTimeUnit.MINUTE).toLocalDateTime(TimeZone.currentSystemDefault())
    }
    return result
}