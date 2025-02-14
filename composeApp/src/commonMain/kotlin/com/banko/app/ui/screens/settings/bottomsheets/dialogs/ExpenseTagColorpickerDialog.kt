package com.banko.app.ui.screens.settings.bottomsheets.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.expense_tag_colorpicker_button_cancel
import banko.composeapp.generated.resources.expense_tag_colorpicker_button_ok
import banko.composeapp.generated.resources.expense_tag_colorpicker_title
import com.banko.app.ui.theme.colorList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseTagColorpickerDialog(
    showColorPicker: MutableState<Boolean>,
    editedColor: MutableState<Long>
) {
    AlertDialog(
        onDismissRequest = { showColorPicker.value = false },
        confirmButton = {
            TextButton(onClick = { showColorPicker.value = false }) {
                Text(
                    text = stringResource(Res.string.expense_tag_colorpicker_button_ok)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { showColorPicker.value = false }) {
                Text(
                    text = stringResource(Res.string.expense_tag_colorpicker_button_cancel)
                )
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.expense_tag_colorpicker_title),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(colorList) { index, color ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .border(BorderStroke(5.dp, MaterialTheme.colorScheme.onSurface))
                            .background(color)
                            .padding(50.dp)
                            .clickable {
                                editedColor.value = color.toArgb().toLong()
                                showColorPicker.value = false
                            }
                    )
                }
            }
        }
    )
}