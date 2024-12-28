package com.banko.app.api.dto.nordigen

import kotlinx.serialization.Serializable

@Serializable
data class Balances(
    val balances: List<Balance>,
)

@Serializable
data class Balance(
    val balanceAmount: BalanceAmount,
    val balanceType: String,
)

@Serializable
data class BalanceAmount(
    val amount: String,
    val currency: String,
)