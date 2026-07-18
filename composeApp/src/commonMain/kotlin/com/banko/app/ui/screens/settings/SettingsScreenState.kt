package com.banko.app.ui.screens.settings

import com.banko.app.api.dto.bankoApi.BankAuthDto
import com.banko.app.domain.model.CurrencyInfo
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.utils.ErrorState

data class SettingsScreenState(
    val expenseTags: List<ExpenseTag> = emptyList(),
    val selectedCurrency: CurrencyInfo = CurrencyInfo("NOK", "Norwegian Krone", "kr"),
    val availableCurrencies: List<CurrencyInfo> = emptyList(),
    val bankAuthorizations: List<BankAuthDto> = emptyList(),
    val isLoadingBanks: Boolean = false,
    val error: ErrorState? = null,
)