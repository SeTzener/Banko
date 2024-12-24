package com.banko.app.ui.components

import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.ic_arrow_drop_down
import banko.composeapp.generated.resources.ic_arrow_drop_up
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ExpandableCard(
    isExpanded: Boolean = false,
    topContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    onAnimationFinished: ((isExpanded: Boolean) -> Unit)? = null,
) {
    val expandedState = remember { mutableStateOf(isExpanded) }
    Column(
        modifier = Modifier.animateContentSize(
            finishedListener = { _, _ -> onAnimationFinished?.invoke(expandedState.value) },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expandedState.value = !expandedState.value })
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                topContent()
            }
            val iconId = if (expandedState.value) Res.drawable.ic_arrow_drop_up else Res.drawable.ic_arrow_drop_down
            Icon(
                modifier = Modifier.border(
                    border =BorderStroke(width = 0.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ),
                imageVector = vectorResource(resource = iconId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (expandedState.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                expandedContent()
            }
        }
    }
}
