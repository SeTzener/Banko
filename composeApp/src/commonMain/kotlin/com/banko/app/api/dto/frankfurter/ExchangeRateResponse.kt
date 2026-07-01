package com.banko.app.api.dto.frankfurter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateResponse(
    val amount: Double,
    val base: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val date: String? = null,
    val rates: Map<String, Map<String, Double>>,
)
