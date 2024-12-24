package com.banko.app.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.banko.app.api.HttpClientProvider
import com.banko.app.api.services.NordigenApiService

@Composable
fun DetailsScreen(component: DetailsComponent, viewModel: DetailScreenViewModel) { // TODO(): Add koin to the project
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(viewModel.screenState.data, color = MaterialTheme.colorScheme.primary)
        Button(onClick = { component.goBack() }) {
            Text("Go Back")
        }
    }
}