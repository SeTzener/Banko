package com.banko.app.domain.model

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
) {
    val displayLabel: String
        get() = when (code) {
            in setOf("NOK", "SEK", "DKK", "ISK") -> code
            in setOf("USD", "CAD", "AUD", "NZD") -> code
            in setOf("CHF") -> code
            else -> symbol
        }
}

fun currencyDisplayForCode(code: String): String {
    return getSupportedCurrencies().find { it.code == code }?.displayLabel ?: code
}

fun getSupportedCurrencies(): List<CurrencyInfo> = listOf(
    CurrencyInfo("EUR", "Euro", "€"),
    CurrencyInfo("USD", "US Dollar", "$"),
    CurrencyInfo("GBP", "British Pound", "£"),
    CurrencyInfo("NOK", "Norwegian Krone", "kr"),
    CurrencyInfo("SEK", "Swedish Krona", "kr"),
    CurrencyInfo("DKK", "Danish Krone", "kr"),
    CurrencyInfo("CHF", "Swiss Franc", "Fr"),
    CurrencyInfo("JPY", "Japanese Yen", "¥"),
    CurrencyInfo("CAD", "Canadian Dollar", "$"),
    CurrencyInfo("AUD", "Australian Dollar", "$"),
    CurrencyInfo("NZD", "New Zealand Dollar", "$"),
    CurrencyInfo("PLN", "Polish Zloty", "zł"),
    CurrencyInfo("CZK", "Czech Koruna", "Kč"),
    CurrencyInfo("HUF", "Hungarian Forint", "Ft"),
    CurrencyInfo("ISK", "Icelandic Krona", "kr"),
)

fun isCurrencySupported(code: String): Boolean =
    getSupportedCurrencies().any { it.code == code }

data class CurrencyPreference(
    val selectedCurrency: String = "NOK",
)
