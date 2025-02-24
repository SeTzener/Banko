package com.banko.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private typealias IconAlign = TextAlign

@Composable
fun TextWithIcon(
    modifier: Modifier = Modifier,
    text: String,
    icon: @Composable () -> Unit,
    iconPadding: Dp = 8.dp,
    iconAlign: IconAlign = IconAlign.Start,
    textAlign: TextAlign = iconAlign,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (textAlign == TextAlign.Start) {
            Arrangement.Start
        } else {
            Arrangement.End
        },
    ) {
        if (iconAlign == IconAlign.Start) {
            icon()
            Spacer(modifier = Modifier.padding(iconPadding))
        }

        Text(
            text = text,
            textAlign = textAlign,
            style = textStyle,
            color = textColor,
        )

        if (iconAlign == IconAlign.End) {
            Spacer(modifier = Modifier.padding(iconPadding))
            icon()
        }
    }
}

@Composable
fun TextWithIcon(
    modifier: Modifier = Modifier,
    text: String,
    iconResId: DrawableResource?,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    iconSize: Dp = 24.dp,
    iconPadding: Dp = 8.dp,
    iconAlign: IconAlign = IconAlign.Start,
    textAlign: TextAlign = iconAlign,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    TextWithIcon(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        textStyle = textStyle,
        textColor = textColor,
        iconPadding = iconPadding,
        iconAlign = iconAlign,
        icon = {
            if (iconResId != null) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(resource = iconResId),
                    contentDescription = null,
                    tint = iconColor,
                )
            } else {
                Spacer(modifier = Modifier.size(iconSize))
            }
        },
    )
}
