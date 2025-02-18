package com.banko.app.ui.screens.details

import com.banko.app.api.dto.bankoApi.ExpenseTag

data class DetailScreenState(
    val expenseTags: List<ExpenseTag> = emptyList()
)