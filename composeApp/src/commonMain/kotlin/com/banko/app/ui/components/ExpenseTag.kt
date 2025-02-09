package com.banko.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.banko.app.ui.models.ExpenseTag

@Composable
fun ExpenseTag(transaction: ExpenseTag?) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (transaction == null) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = transaction.color,
                        shape = MaterialTheme.shapes.small,
                    )
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}