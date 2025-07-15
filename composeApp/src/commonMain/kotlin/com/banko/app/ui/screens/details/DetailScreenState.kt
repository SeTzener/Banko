package com.banko.app.ui.screens.details

import com.banko.app.ui.models.ExpenseTag

data class DetailScreenState(
    val expenseTags: List<ExpenseTag> = emptyList(),
    val error: String? = null,
)

sealed class TransactionsEvent {
    // TODO(): implement error showing
    data class ErrorShown(val error: String) : TransactionsEvent()
}