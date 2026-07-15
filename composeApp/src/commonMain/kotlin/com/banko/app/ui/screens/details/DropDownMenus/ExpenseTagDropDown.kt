package com.banko.app.ui.screens.details.DropDownMenus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.details_expense_tag_empty
import banko.composeapp.generated.resources.ic_tag
import banko.composeapp.generated.resources.ic_tag_filled
import com.banko.app.ui.models.ExpenseTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpenseTagDropdown(
    expanded: MutableState<Boolean>,
    expenseTags: List<ExpenseTag>,
    onTagSelected: (ExpenseTag?) -> Unit
) {
    if (!expanded.value) return

    val maxVisibleItems = 7
    val itemHeight = 48.dp
    val surfaceColor = MaterialTheme.colorScheme.onSurface
    val scrollState = rememberScrollState()

    val totalItems = expenseTags.size + 1
    val isScrollable = totalItems > maxVisibleItems
    val canScrollUp = scrollState.value > 0
    val canScrollDown = isScrollable && scrollState.value < scrollState.maxValue

    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = { expanded.value = false }
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(4.dp),
            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        ) {
            Box(
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = itemHeight * maxVisibleItems)
                        .verticalScroll(scrollState)
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_tag),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.details_expense_tag_empty),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            onTagSelected(null)
                            expanded.value = false
                        })
                    expenseTags.forEach { tag ->
                        DropdownMenuItem(leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_tag_filled),
                                contentDescription = null,
                                tint = tag.color
                            )
                        }, text = {
                            Text(
                                text = tag.name, color = MaterialTheme.colorScheme.primary
                            )
                        }, onClick = {
                            onTagSelected(tag)
                            expanded.value = false
                        })
                    }
                }

                if (canScrollUp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(surfaceColor, surfaceColor.copy(alpha = 0f))
                                )
                            )
                    )
                }

                if (canScrollDown) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(surfaceColor.copy(alpha = 0f), surfaceColor)
                                )
                            )
                    )
                }
            }
        }
    }
}