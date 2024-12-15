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
import com.banko.app.ui.models.Category
import com.banko.app.ui.theme.Grey_Nevada

@Composable
fun CircularIndicator(
    currency: String,
    monthlyBudget: Int = 0,
    dailyBudgetText: String,
    monthlyBudgetText: String,
    categories: List<Category>,
    bigTextFontSize: TextUnit = MaterialTheme.typography.titleLarge.fontSize,
    bigTextColor: Color = MaterialTheme.colorScheme.onBackground,
    canvasSize: Dp = 300.dp,
    indicatorStroke: Float = 60f,
    dailyBudget: Int = 0,
    smallTextColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
    smallTextFontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
) {
    val totalAmount = categories.sumOf { it.amount.toInt() }.toFloat()
    var animatedMonthlyBudgetValue by remember { mutableIntStateOf(0) }
    var animatedDailyBudgetValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(dailyBudget) {
        animatedMonthlyBudgetValue = monthlyBudget
    }
    LaunchedEffect(dailyBudget) {
        animatedDailyBudgetValue = dailyBudget
    }

    val animatedDailyBudget by animateIntAsState(
        targetValue = animatedDailyBudgetValue,
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
                dailyBudget = animatedDailyBudget,
                monthlyBudget = animatedMonthlyBudget,
                bigTextFontSize = bigTextFontSize,
                bigTextColor = bigTextColor,
                currency = currency,
                dailyBudgetText = dailyBudgetText,
                monthlyBudgetText = monthlyBudgetText,
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
    monthlyBudgetText: String,
    dailyBudgetText: String,
    smallTextColor: Color,
    smallTextFontSize: TextUnit
) {
    Text(
        text = monthlyBudgetText,
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
        text = dailyBudgetText,
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
            .fillMaxWidth().offset(y = (-60).dp),
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
                            text = category.amount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}