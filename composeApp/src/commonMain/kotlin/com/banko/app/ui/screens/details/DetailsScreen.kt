package com.banko.app.ui.screens.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.details
import banko.composeapp.generated.resources.details_button_back
import com.banko.app.ui.models.Transaction
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailsScreen(component: DetailsComponent, transaction: Transaction) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Start)
                .padding(16.dp),
            text = stringResource(Res.string.details),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium
        )
        Column (
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            Text(
                text = transaction.remittanceInformationUnstructuredArray.joinToString(" "),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { component.goBack() }
        ) {
            Text(stringResource(Res.string.details_button_back))
        }
    }
}