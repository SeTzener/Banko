package com.banko.app.ui.screens.settings

import com.banko.app.ui.models.ExpenseTag

data class SettingsScreenState(
    val expenseTags: List<ExpenseTag> = emptyList(),
)