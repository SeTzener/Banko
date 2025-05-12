package com.banko.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.banko.app.utils.beginningOfCurrentMonth
import kotlinx.datetime.LocalDateTime

@Composable
fun MonthYearPickerDialog(
    visible: Boolean,
    startYear: Int,
    startMonth: Int,
    onConfirm: (LocalDateTime) -> Unit,
    onCancel: () -> Unit
) {
    if (!visible) return

    val today = beginningOfCurrentMonth()
    val currentYear = today.year
    val currentMonth = today.month.ordinal
    val months = listOf(
        "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    )

    val years = (startYear..currentYear).toList()
    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }

    val monthScrollState = rememberLazyListState(selectedMonth - 1)
    val yearScrollState = rememberLazyListState(years.size - 1)

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.height(150.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    LazyColumn(
                        state = monthScrollState,
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(months) { index, month ->
                            val valid = when (selectedYear) {
                                startYear -> index + 1 >= startMonth
                                currentYear -> index + 1 <= currentMonth
                                else -> true
                            }
                            Text(
                                text = month,
                                color = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                fontWeight = if (index + 1 == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .clickable(enabled = valid) {
                                        selectedMonth = index + 1
                                    }
                            )
                        }
                    }
                    LazyColumn(
                        state = yearScrollState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(years) { year: Int ->
                            Text(
                                text = year.toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        selectedYear = year
                                        // Clamp selected month
                                        selectedMonth = selectedMonth.coerceIn(
                                            when (year) {
                                                startYear -> startMonth..12
                                                currentYear -> 1..currentMonth
                                                else -> 1..12
                                            }
                                        )
                                    }
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    TextButton(onClick = {
                        onConfirm(LocalDateTime(selectedYear, selectedMonth, 1, 0, 0))
                    }) { Text("OK") }
                }
            }
        }
    }
}
