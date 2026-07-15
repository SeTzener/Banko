package com.banko.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.account_balance
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource

@Composable
fun BankLogo(
    logoUrl: String?,
    bankName: String?,
    modifier: Modifier = Modifier,
    size: Int = 22
) {
    val sizeDp = size.dp

    if (!logoUrl.isNullOrBlank()) {
        AsyncImage(
            model = logoUrl,
            contentDescription = bankName,
            modifier = modifier
                .size(sizeDp)
                .clip(CircleShape)
        )
    } else if (!bankName.isNullOrBlank()) {
        val initial = bankName.first().uppercase()
        val color = bankNameToColor(bankName)
        Box(
            modifier = modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = (size * 0.5).sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(Res.drawable.account_balance),
                contentDescription = null,
                modifier = Modifier.size((size * 0.55).dp)
            )
        }
    }
}

private fun bankNameToColor(name: String): Color {
    val hash = name.hashCode()
    val hue = ((hash and 0xFFFFFF).toFloat() % 360f + 360f) % 360f
    return Color.hsl(hue, saturation = 0.55f, lightness = 0.45f)
}
