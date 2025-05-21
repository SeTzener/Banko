package com.banko.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.monthly_earnings
import banko.composeapp.generated.resources.monthly_spendings
import com.banko.app.ModelTransaction
import com.banko.app.ui.models.Category
import com.banko.app.ui.models.Transaction
import com.banko.app.ui.screens.home.HomeScreenState
import com.banko.app.ui.theme.Grey_Nevada
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

@Composable
fun CircularIndicator(
    currency: String,
    monthlyTransactions: List<ModelTransaction>,
    bigTextFontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    bigTextColor: Color = MaterialTheme.colorScheme.primary,
    canvasSize: Dp = 256.dp,
    indicatorStroke: Float = 60f,
    smallTextColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    smallTextFontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    indicatorDateState: LocalDateTime
) {
    val monthlySpending = monthlyTransactions.filter {
        it.bookingDate.month == indicatorDateState.month && it.expenseTag?.name != "Salary"
    }.sumOf { it.amount }.toInt()
    val monthlyEarnings = monthlyTransactions.filter {
        it.bookingDate.month == indicatorDateState.month && it.expenseTag?.name == "Salary"
    }.sumOf { it.amount }.toInt()
    val categories = sortCategories(monthlyTransactions, month = indicatorDateState.month)
    val totalAmount = categories.sumOf { it.amount.toInt() }.toFloat()
    var animatedMonthlyBudgetValue by remember { mutableIntStateOf(0) }
    var animatedMonthlySpendingsValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(monthlyEarnings) {
        animatedMonthlyBudgetValue = monthlyEarnings
    }
    LaunchedEffect(monthlySpending) {
        animatedMonthlySpendingsValue = monthlySpending
    }

    val animatedMonthlySpendings by animateIntAsState(
        targetValue = animatedMonthlySpendingsValue,
        animationSpec = tween(1000),
        label = ""
    )

    val animatedMonthlyBudget by animateIntAsState(
        targetValue = animatedMonthlyBudgetValue,
        animationSpec = tween(1000),
        label = ""
    )

    // Remember and animate the sweep angles for each category
    val animatedSweepAngles = categories.mapIndexed { index, category ->
        animateFloatAsState(
            targetValue = (category.amount.toInt() / totalAmount) * 240f,
            animationSpec = tween(1000),
            label = ""
        ).value
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .size(canvasSize)
                .drawBehind {
                    val componentSize = size / 1.25f
                    backgroundIndicator(
                        componentSize = componentSize,
                        indicatorStroke = indicatorStroke,
                        indicatorColor = Grey_Nevada
                    )
                    foregroundIndicator(
                        categories = categories,
                        animatedSweepAngles = animatedSweepAngles,
                        startAngle = 150f, // È il punto da cui parte il tag
                        componentSize = componentSize,
                        indicatorStroke = indicatorStroke,
                    )
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmbeddedElements(
                dailyBudget = animatedMonthlySpendings,
                monthlyBudget = animatedMonthlyBudget,
                bigTextFontSize = bigTextFontSize,
                bigTextColor = bigTextColor,
                currency = currency,
                monthlySpendings = stringResource(Res.string.monthly_spendings),
                monthlyEarnings = stringResource(Res.string.monthly_earnings),
                smallTextColor = smallTextColor,
                smallTextFontSize = smallTextFontSize,
            )
        }
        CategoryGrid(categories = categories)
    }
}

fun DrawScope.foregroundIndicator(
    categories: List<Category>,
    animatedSweepAngles: List<Float>,
    componentSize: Size,
    indicatorStroke: Float,
    startAngle: Float,
) {
    var currentStartAngle = startAngle
    categories.forEachIndexed { index, category ->
        drawArc(
            size = componentSize,
            color = category.color,
            startAngle = currentStartAngle,
            sweepAngle = animatedSweepAngles[index],
            useCenter = false,
            style = Stroke(
                width = indicatorStroke,
                cap = StrokeCap.Square
            ),
            topLeft = Offset(
                (size.width - componentSize.width) / 2f,
                (size.height - componentSize.height) / 2f
            )
        )
        currentStartAngle += animatedSweepAngles[index]
    }
}

fun DrawScope.backgroundIndicator(
    componentSize: Size,
    indicatorStroke: Float,
    indicatorColor: Color,
) {
    drawArc(
        size = componentSize,
        color = indicatorColor,
        startAngle = 150f,
        sweepAngle = 240f,
        useCenter = false,
        style = Stroke(
            width = indicatorStroke,
            cap = StrokeCap.Square
        ),
        topLeft = Offset(
            x = (size.width - componentSize.width) / 2f,
            y = (size.height - componentSize.height) / 2f
        )
    )
}

@Composable
private fun EmbeddedElements(
    dailyBudget: Int,
    monthlyBudget: Int,
    bigTextFontSize: TextUnit,
    bigTextColor: Color,
    currency: String,
    monthlyEarnings: String,
    monthlySpendings: String,
    smallTextColor: Color,
    smallTextFontSize: TextUnit,
) {
    Text(
        text = monthlyEarnings,
        color = smallTextColor,
        fontSize = smallTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "$monthlyBudget $currency",
        color = bigTextColor,
        fontSize = bigTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = monthlySpendings,
        color = smallTextColor,
        fontSize = smallTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "$dailyBudget $currency",
        color = bigTextColor,
        fontSize = bigTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CategoryGrid(categories: List<Category>, itemsPerRow: Int = 5) {
    Column(
        modifier = Modifier
            .fillMaxWidth().offset(y = (-30).dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Break the list into chunks of `itemsPerRow`
        categories.chunked(itemsPerRow).forEach { rowCategories ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowCategories.forEach { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = category.color,
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = category.amount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

private fun sortCategories(transactions: List<ModelTransaction>, month: Month): List<Category> {
    val result =
        transactions.filter { it.expenseTag != null && it.bookingDate.month == month && it.expenseTag.name != "Salary" }
    return result.groupBy { it.expenseTag }.map {
        val sum = it.value.sumOf { it.amount }
        val roundedAmount = (sum * 100).roundToLong() / 100.0
        Category(
            name = it.key!!.name,
            amount = roundedAmount,
            color = it.key!!.color
        )
    }
}