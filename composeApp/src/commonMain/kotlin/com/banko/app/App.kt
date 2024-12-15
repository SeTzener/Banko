package com.banko.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.currency_nok
import banko.composeapp.generated.resources.daily_budget
import banko.composeapp.generated.resources.monthly_budget
import com.banko.app.ui.components.CircularIndicator
import com.banko.app.ui.models.categories
import com.banko.app.ui.theme.BankoTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
@Preview
fun App() {
    BankoTheme {
        Surface {
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
                            categories = categories.take(Random.nextInt(categories.size -1))
                        )
                    }
                }
            }
        }
    }
}