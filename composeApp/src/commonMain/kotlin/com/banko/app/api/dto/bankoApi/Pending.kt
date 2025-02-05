package com.banko.app.api.dto.bankoApi

import kotlinx.serialization.Serializable

@Serializable
data class Pending(
    val id: String,
    val bookingDate: String,
    val amount: String,
    val currency: String,
    val remittanceInformationUnstructured: String,
    val remittanceInformationUnstructuredArray: List<String>
)