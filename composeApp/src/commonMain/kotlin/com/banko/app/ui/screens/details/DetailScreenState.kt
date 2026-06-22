package com.banko.app.ui.screens.details

import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.utils.ErrorState

data class DetailScreenState(
    val expenseTags: List<ExpenseTag> = emptyList(),
    val error: ErrorState? = null,
)