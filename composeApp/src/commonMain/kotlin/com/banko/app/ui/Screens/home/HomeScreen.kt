package com.banko.app.ui.Screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.currency_nok
import banko.composeapp.generated.resources.daily_budget
import banko.composeapp.generated.resources.monthly_budget
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.banko.app.ui.components.CircularIndicator
import com.banko.app.ui.models.categories
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
fun HomeScreen(component: HomeComponent) {
    val text by component.text.subscribeAsState()
    var showContent by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showContent = !showContent }) {
            Text("Click me!")
        }
        AnimatedVisibility(showContent) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularIndicator(
                    dailyBudget = Random.nextInt(100),
                    currency = stringResource(Res.string.currency_nok),
                    dailyBudgetText = stringResource(Res.string.daily_budget),
                    monthlyBudgetText = stringResource(Res.string.monthly_budget),
                    monthlyBudget = 123456789,
                    categories = categories.take(Random.nextInt(categories.size - 1))
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { component.onEvent(HomeEvent.UpdateText(it)) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
                Button(onClick = { component.onEvent(HomeEvent.buttonClick) }) {
                    Text("Go to details")
                }
            }
        }
    }

}