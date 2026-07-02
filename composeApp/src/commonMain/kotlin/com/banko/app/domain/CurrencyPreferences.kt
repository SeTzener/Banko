package com.banko.app.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrencyPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val SELECTED_CURRENCY_KEY = stringPreferencesKey("selected_currency")
        const val DEFAULT_CURRENCY = "NOK"
    }

    val selectedCurrency: Flow<String> = dataStore.data.map { preferences ->
        preferences[SELECTED_CURRENCY_KEY] ?: DEFAULT_CURRENCY
    }

    suspend fun setSelectedCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY_KEY] = currency
        }
    }
}
