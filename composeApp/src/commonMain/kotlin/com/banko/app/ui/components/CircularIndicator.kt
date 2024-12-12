package com.banko.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

@Composable
fun CircularIndicator(
    bigTextFontSize: TextUnit = MaterialTheme.typography.h2.fontSize,
    bigTextSuffix: String,
    backgroundIndicatorColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.99f),
    bigTextColor: Color = MaterialTheme.colors.onSurface, // forse questo lo posso cambiare per metterci i vari colori
    canvasSize: Dp = 300.dp, // Da adattare alla misura che voglio realmente nello schermo
    foregroundColor: Color = Color.Blue,
    indicatorStroke: Float = 100f,
    indicatorValue: Int = 0,
    maxIndicatorValue: Int = 100,
    smallText: String,
    smallTextColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
    smallTextFontSize: TextUnit = MaterialTheme.typography.body1.fontSize,
) {
    var allowedIndicatorValue by remember {
        mutableIntStateOf(maxIndicatorValue)
    }
    // TODO(): Questo blocco non serve
    // ----------------------------------------------------
    allowedIndicatorValue = if (indicatorValue <= maxIndicatorValue) {
        indicatorValue
    } else {
        maxIndicatorValue
    }
    // ----------------------------------------------------
    var animatedIndicatorValue by remember {
        mutableFloatStateOf(0f)
    }
    LaunchedEffect(key1 = allowedIndicatorValue) {
        animatedIndicatorValue = allowedIndicatorValue.toFloat()
    }
    val percentage = (animatedIndicatorValue / maxIndicatorValue) * 100

    // The arc begins at startAngle and spans sweepAngle degrees counterclockwise.
    // A value of 360 degrees would draw a full circle.
    val sweepAngle by animateFloatAsState(
        targetValue = (2.4 * percentage).toFloat(),
        animationSpec = tween(1000),
        label = ""
    )
    val receivedValue by animateIntAsState(
        targetValue = allowedIndicatorValue,
        animationSpec = tween(1000),
        label = ""
    )
    // TODO(): che testo colora sto coso?
    val animatedBigTextColor by animateColorAsState(
        targetValue = if (allowedIndicatorValue == 0) {
            MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
        } else {
            bigTextColor
        },
        animationSpec = tween(1000),
        label = ""
    )

    Column(
        modifier = Modifier
            .size(canvasSize)
            .drawBehind {
                val componentSize = size / 1.25f
                backgroundIndicator(
                    componentSize = componentSize,
                    indicatorColor = backgroundIndicatorColor,
                    indicatorStrokeWidth = indicatorStroke
                )
                foregroundIndicator(
                    startAngle = 150f, // È il punto da cui parte il tag
                    sweepAngle = sweepAngle,
                    componentSize = componentSize,
                    indicatorColor = foregroundColor,
                    indicatorStrokeWidth = indicatorStroke
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmbeddedElements(
            bigText = receivedValue,
            bigTextFontSize = bigTextFontSize,
            bigTextColor = animatedBigTextColor,
            bigTextSuffix = bigTextSuffix,
            smallText = smallText,
            smallTextColor = smallTextColor,
            smallTextFontSize = smallTextFontSize,
        )
    }
}

/**
 * Draws the shape of the indicator's foreground as an arc.
 *
 * This function uses the given size, color, and stroke width to draw a rounded arc
 * that serves as the background for an indicator component. The arc is drawn on the
 * [DrawScope] at a fixed angle and position based on the component size.
 *
 * @param componentSize The size of the indicator background to be drawn.
 * @param indicatorColor The color of the indicator's background.
 * @param indicatorStrokeWidth The stroke width used to draw the indicator's background.
 */
private fun DrawScope.foregroundIndicator(
    startAngle: Float,
    sweepAngle: Float,
    componentSize: Size,
    indicatorColor: Color,
    indicatorStrokeWidth: Float
) {
    drawArc(
        size = componentSize,
        color = indicatorColor,
        // this is the point where the arc starts.
        startAngle = startAngle,
        // The arc begins at startAngle and spans sweepAngle degrees counterclockwise.
        // A value of 360 degrees would draw a full circle.
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = indicatorStrokeWidth,
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            x = (size.width - componentSize.width) / 2f,
            y = (size.height - componentSize.height) / 2f,
        )
    )

}

/**
 * Draws the shape of the indicator's background as an arc.
 *
 * This function uses the given size, color, and stroke width to draw a rounded arc
 * that serves as the background for an indicator component. The arc is drawn on the
 * [DrawScope] at a fixed angle and position based on the component size.
 *
 * @param componentSize The size of the indicator background to be drawn.
 * @param indicatorColor The color of the indicator's background.
 * @param indicatorStrokeWidth The stroke width used to draw the indicator's background.
 */
private fun DrawScope.backgroundIndicator(
    componentSize: Size,
    indicatorColor: Color,
    indicatorStrokeWidth: Float
) {
    drawArc(
        size = componentSize,
        color = indicatorColor,
        // this is the point where the arc starts.
        startAngle = 150f,
        // The arc begins at startAngle and spans sweepAngle degrees counterclockwise.
        // A value of 360 degrees would draw a full circle.
        sweepAngle = 240f,
        useCenter = false,
        style = Stroke(
            width = indicatorStrokeWidth,
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            x = (size.width - componentSize.width) / 2f,
            y = (size.height - componentSize.height) / 2f,
        )
    )
}

@Composable
fun EmbeddedElements(
    bigText: Int,
    bigTextFontSize: TextUnit,
    bigTextColor: Color,
    bigTextSuffix: String,
    smallText: String,
    smallTextColor: Color,
    smallTextFontSize: TextUnit
){
    Text(
        text = smallText,
        color = smallTextColor,
        fontSize = smallTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "$bigText ${bigTextSuffix.take(2)}",
        color = bigTextColor,
        fontSize = bigTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}