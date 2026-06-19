package com.banko.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
import banko.composeapp.generated.resources.expense_tag_Other
import banko.composeapp.generated.resources.expense_tag_uncategorized
import banko.composeapp.generated.resources.monthly_earnings
import banko.composeapp.generated.resources.monthly_spendings
import banko.composeapp.generated.resources.yearly_earnings
import banko.composeapp.generated.resources.yearly_spendings
import com.banko.app.ModelTransaction
import com.banko.app.ui.models.Category
import com.banko.app.ui.models.Transaction
import com.banko.app.ui.screens.home.TimespanSelection
import com.banko.app.ui.theme.Grey_Nevada
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun CircularIndicator(
    currency: String,
    transactions: List<ModelTransaction>,
    selectedTimespan: TimespanSelection,
    bigTextFontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    bigTextColor: Color = MaterialTheme.colorScheme.primary,
    canvasSize: Dp = 256.dp,
    indicatorStroke: Float = 60f,
    smallTextColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    smallTextFontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    onCategoryClick: ((String?) -> Unit)? = null,
    selectedCategoryId: String? = null,
    isUncategorizedSelected: Boolean = false,
) {
    val (transactionsInRange, isYearView) = when (selectedTimespan) {
        is TimespanSelection.Month -> {
            Pair(
                transactions.filter {
                    it.bookingDate.monthNumber == selectedTimespan.ym.month &&
                            it.bookingDate.year == selectedTimespan.ym.year
                },
                false
            )
        }
        is TimespanSelection.Year -> {
            Pair(
                transactions.filter { it.bookingDate.year == selectedTimespan.year },
                true
            )
        }
    }

    val totalSpending = transactionsInRange.filter {
        it.expenseTag?.isEarning != true
    }.sumOf { it.amount }.toInt()
    val totalEarnings = transactionsInRange.filter {
        it.expenseTag?.isEarning == true
    }.sumOf { it.amount }.toInt()
    val otherLabel = stringResource(Res.string.expense_tag_Other)
    val categories = remember(transactions, selectedTimespan) {
        when (selectedTimespan) {
            is TimespanSelection.Month -> sortCategories(transactions, month = Month(selectedTimespan.ym.month), otherLabel = otherLabel)
            is TimespanSelection.Year -> sortCategories(transactions, year = selectedTimespan.year, otherLabel = otherLabel)
        }
    }
    val totalAmount = categories.sumOf { it.amount }.toFloat()
    var animatedEarningsValue by remember { mutableIntStateOf(0) }
    var animatedSpendingsValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(totalEarnings) {
        animatedEarningsValue = totalEarnings
    }
    LaunchedEffect(totalSpending) {
        animatedSpendingsValue = totalSpending
    }

    val animatedSpendings by animateIntAsState(
        targetValue = animatedSpendingsValue,
        animationSpec = tween(1000),
        label = ""
    )

    val animatedEarnings by animateIntAsState(
        targetValue = animatedEarningsValue,
        animationSpec = tween(1000),
        label = ""
    )

    val animatedSweepAngles = if (totalAmount == 0f) {
        categories.map { 0f }
    } else {
        val rawAngles = categories.map { category ->
            animateFloatAsState(
                targetValue = (category.amount.toFloat() / totalAmount) * 240f,
                animationSpec = tween(1000),
                label = ""
            ).value
        }
        rawAngles.toMutableList().also { angles ->
            if (angles.isNotEmpty()) {
                val sum = angles.sum()
                angles[angles.lastIndex] += 240f - sum
            }
        }
    }

    val spendingsLabel = if (isYearView) stringResource(Res.string.yearly_spendings) else stringResource(Res.string.monthly_spendings)
    val earningsLabel = if (isYearView) stringResource(Res.string.yearly_earnings) else stringResource(Res.string.monthly_earnings)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)

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
                        startAngle = 150f,
                        componentSize = componentSize,
                        indicatorStroke = indicatorStroke,
                        primaryColor = primaryColor,
                        surfaceColor = surfaceColor,
                    )
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmbeddedElements(
                dailyBudget = animatedSpendings,
                monthlyBudget = animatedEarnings,
                bigTextFontSize = bigTextFontSize,
                bigTextColor = bigTextColor,
                currency = currency,
                monthlySpendings = spendingsLabel,
                monthlyEarnings = earningsLabel,
                smallTextColor = smallTextColor,
                smallTextFontSize = smallTextFontSize,
            )
        }
        CategoryGrid(
            categories = categories,
            onCategoryClick = onCategoryClick,
            selectedCategoryId = selectedCategoryId,
            isUncategorizedSelected = isUncategorizedSelected,
        )
    }
}

fun DrawScope.foregroundIndicator(
    categories: List<Category>,
    animatedSweepAngles: List<Float>,
    componentSize: Size,
    indicatorStroke: Float,
    startAngle: Float,
    primaryColor: Color,
    surfaceColor: Color,
) {
    var currentStartAngle = startAngle
    categories.forEachIndexed { index, category ->
        val isOther = category.id == null
        val arcTopLeft = Offset(
            (size.width - componentSize.width) / 2f,
            (size.height - componentSize.height) / 2f
        )

        if (isOther) {
            drawArc(
                color = surfaceColor,
                startAngle = currentStartAngle,
                sweepAngle = animatedSweepAngles[index],
                useCenter = false,
                size = componentSize,
                style = Stroke(
                    width = indicatorStroke,
                    cap = StrokeCap.Square
                ),
                topLeft = arcTopLeft
            )

            drawArc(
                color = primaryColor,
                startAngle = currentStartAngle,
                sweepAngle = animatedSweepAngles[index],
                useCenter = false,
                size = componentSize,
                style = Stroke(
                    width = indicatorStroke * 0.15f,
                    cap = StrokeCap.Square
                ),
                topLeft = arcTopLeft
            )
        } else {
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
                topLeft = arcTopLeft
            )
        }

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
private fun CategoryGrid(
    categories: List<Category>,
    itemsPerRow: Int = 5,
    onCategoryClick: ((String?) -> Unit)? = null,
    selectedCategoryId: String? = null,
    isUncategorizedSelected: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().offset(y = (-30).dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.chunked(itemsPerRow).forEach { rowCategories ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowCategories.forEach { category ->
                    val isOther = category.id == null
                    val isSelected = if (isOther) isUncategorizedSelected
                    else category.id == selectedCategoryId
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                } else Modifier
                            )
                            .clickable { onCategoryClick?.invoke(category.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (isOther) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = category.color,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                        }
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

private fun sortCategories(transactions: List<ModelTransaction>, month: kotlinx.datetime.Month, otherLabel: String): List<Category> {
    val result =
        transactions.filter { it.bookingDate.month == month && it.expenseTag?.isEarning != true }
    return result.groupBy { it.expenseTag }.mapNotNull {
        val sum = it.value.sumOf { it.amount }
        if (sum >= 0) return@mapNotNull null
        val roundedAmount = (abs(sum) * 100).roundToLong() / 100.0
        val tag = it.key
        if (tag != null) {
            Category(
                id = tag.id,
                name = tag.name,
                amount = roundedAmount,
                color = tag.color
            )
        } else {
            Category(
                id = null,
                name = otherLabel,
                amount = roundedAmount,
                color = Color.Gray
            )
        }
    }
}

private fun sortCategories(transactions: List<ModelTransaction>, year: Int, otherLabel: String): List<Category> {
    val result =
        transactions.filter { it.bookingDate.year == year && it.expenseTag?.isEarning != true }
    return result.groupBy { it.expenseTag }.mapNotNull {
        val sum = it.value.sumOf { it.amount }
        if (sum >= 0) return@mapNotNull null
        val roundedAmount = (abs(sum) * 100).roundToLong() / 100.0
        val tag = it.key
        if (tag != null) {
            Category(
                id = tag.id,
                name = tag.name,
                amount = roundedAmount,
                color = tag.color
            )
        } else {
            Category(
                id = null,
                name = otherLabel,
                amount = roundedAmount,
                color = Color.Gray
            )
        }
    }
}
