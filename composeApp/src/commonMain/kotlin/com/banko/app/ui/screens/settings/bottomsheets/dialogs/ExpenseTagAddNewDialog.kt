package com.banko.app.ui.screens.settings.bottomsheets.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.expense_tag_add_new_button_cancel
import banko.composeapp.generated.resources.expense_tag_add_new_button_save
import banko.composeapp.generated.resources.expense_tag_add_new_text
import banko.composeapp.generated.resources.expense_tag_add_new_title
import banko.composeapp.generated.resources.ic_tag
import com.banko.app.api.dto.bankoApi.ExpenseTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseTagAddNewDialog(
    isNewTag: MutableState<Boolean>,
    showColorPicker: MutableState<Boolean>,
    onTagCreate: (name: String, color: Long) -> Unit
) {
    val newName = remember { mutableStateOf("") }
    val color = remember { mutableStateOf<Long>(0) }
    var isError by remember { mutableStateOf(false) }

    if (showColorPicker.value) {
        ExpenseTagColorpickerDialog(
            showColorPicker = showColorPicker,
            editedColor = color
        )
    }
    AlertDialog(
        onDismissRequest = { isNewTag.value = false }, // Close dialog on outside click
        title = {
            Text(
                text = stringResource(Res.string.expense_tag_add_new_title),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.expense_tag_add_new_text),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 25.dp, bottom = 15.dp, start = 5.dp, end = 5.dp)
            ) {
                IconButton(
                    onClick = { showColorPicker.value = true },
                    modifier = Modifier.padding(end = 8.dp).align(Alignment.Bottom),
                ) {
                    Icon(
                        modifier = Modifier.padding(
                            start = 8.dp,
                            top = 8.dp,
                            end = 12.dp,
                            bottom = 8.dp
                        ),
                        painter = painterResource(resource = Res.drawable.ic_tag),
                        contentDescription = null,
                        tint = if (color.value == 0L) {
                            Color.White.copy(alpha = 0.5f)
                        } else {
                            Color(color.value)
                        },
                    )
                }

                TextField(
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    value = newName.value,
                    onValueChange = { newName.value = it },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    isError = isError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        errorTextColor = MaterialTheme.colorScheme.primary,
                        errorPlaceholderColor = MaterialTheme.colorScheme.error
                    ),
                    shape =
                    MaterialTheme.shapes.small.copy(
                        bottomEnd = ZeroCornerSize,
                        bottomStart = ZeroCornerSize
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (color.value == 0L) {
                        color.value = Color.Red.toArgb().toLong()
                    }
                    if (newName.value.isEmpty()) {
                        isError = true
                    }
                    if (color.value != 0L && newName.value.isNotEmpty()) {
                        onTagCreate(newName.value, color.value)
                        isNewTag.value = false // Close the dialog
                    }
                }
            ) {
                Text(
                    text = stringResource(Res.string.expense_tag_add_new_button_save),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { isNewTag.value = false } // Close the dialog
            ) {
                Text(
                    text = stringResource(Res.string.expense_tag_add_new_button_cancel),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    )
}