package com.banko.app.ui.screens.details

import com.banko.app.ui.models.ExpenseTag

data class DetailScreenState(
    val expenseTags: List<ExpenseTag> = emptyList(),
    val error: String? = null,
    val rawError: String? = null,
)