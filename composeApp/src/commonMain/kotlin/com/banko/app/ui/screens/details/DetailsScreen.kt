package com.banko.app.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailsScreen(component: DetailsComponent, viewModel: DetailsScreenViewModel) {
    var isDataLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        isDataLoading = viewModel.screenState.data.isEmpty()
        if (isDataLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(35.dp),
                strokeWidth = 5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(viewModel.screenState.data, color = MaterialTheme.colorScheme.primary)
        Button(onClick = { component.goBack() }) {
            Text("Go Back")
        }
    }
}