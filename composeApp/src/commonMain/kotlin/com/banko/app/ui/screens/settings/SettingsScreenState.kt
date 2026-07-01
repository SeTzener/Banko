package com.banko.app.ui.screens.settings

import com.banko.app.domain.model.CurrencyInfo
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.utils.ErrorState

data class SettingsScreenState(
    val expenseTags: List<ExpenseTag> = emptyList(),
    val selectedCurrency: CurrencyInfo = CurrencyInfo("NOK", "Norwegian Krone", "kr"),
    val availableCurrencies: List<CurrencyInfo> = emptyList(),
    val error: ErrorState? = null,
)