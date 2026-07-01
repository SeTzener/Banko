package com.banko.app.ui.screens.settings.bottomsheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.banko.app.domain.model.CurrencyInfo

@Composable
fun CurrencyPickerSheet(
    currencies: List<CurrencyInfo>,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        currencies.forEach { currency ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCurrencySelected(currency.code) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = currency.code == selectedCurrency,
                    onClick = { onCurrencySelected(currency.code) },
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "${currency.symbol}  ${currency.code} — ${currency.name}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
