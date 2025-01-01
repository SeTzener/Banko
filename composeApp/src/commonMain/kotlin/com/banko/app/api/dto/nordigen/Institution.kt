package com.banko.app.api.dto.nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Institutions(
    val id: String,
    val name: String,
    val bic: String,
    @SerialName("transaction_total_days")
    val transactionTotalDays: String,
    val countries: List<String>,
    val logo: String,
    @SerialName("max_access_valid_for_days")
    val maxAccessValidForDays: String,
)