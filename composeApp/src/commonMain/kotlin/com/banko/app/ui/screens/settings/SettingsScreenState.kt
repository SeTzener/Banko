package com.banko.app.ui.screens.settings

import com.banko.app.api.dto.bankoApi.ExpenseTag

data class SettingsScreenState(
    val expenseTags: List<ExpenseTag> = emptyList(),
)