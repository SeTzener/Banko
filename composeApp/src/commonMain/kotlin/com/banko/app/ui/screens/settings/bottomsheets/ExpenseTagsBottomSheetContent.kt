package com.banko.app.ui.screens.settings.bottomsheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.expense_tag_add_new
import banko.composeapp.generated.resources.expense_tag_update_earning
import banko.composeapp.generated.resources.expense_tag_update_expense
import banko.composeapp.generated.resources.expense_tags_bottom_sheet_button_close
import banko.composeapp.generated.resources.expense_tags_title
import banko.composeapp.generated.resources.ic_delete
import banko.composeapp.generated.resources.ic_edit
import banko.composeapp.generated.resources.ic_save
import banko.composeapp.generated.resources.ic_tag_filled
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.screens.settings.SettingsScreenState
import com.banko.app.ui.screens.settings.bottomsheets.dialogs.ExpenseTagAddNewDialog
import com.banko.app.ui.screens.settings.bottomsheets.dialogs.ExpenseTagColorpickerDialog
import com.banko.app.ui.screens.settings.bottomsheets.dialogs.ExpenseTagDeleteDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseTagsBottomSheetContent(
    screenState: SettingsScreenState,
    loadNewTags: () -> Unit,
    onTagUpdate: (ExpenseTag) -> Unit,
    onTagCreate: (name: String, color: Color, isEarning: Boolean) -> Unit,
    onTagDelete: (expanseTagId: String) -> Unit,
    onClose: () -> Unit
) {

    loadNewTags()

    val isNewTag = remember { mutableStateOf(false) }
    val showColorPicker = remember { mutableStateOf(false) }

    if (isNewTag.value) {
        ExpenseTagAddNewDialog(
            isNewTag = isNewTag,
            showColorPicker = showColorPicker,
            onTagCreate = onTagCreate
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .weight(1f) // Take up all available space
        ) {
            Row {
                Text(
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp).weight(1f)
                        .align(Alignment.CenterVertically),
                    text = stringResource(Res.string.expense_tags_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    modifier = Modifier.padding(end = 12.dp).align(Alignment.CenterVertically),
                    onClick = { isNewTag.value = true },
                ) {
                    Text(
                        text = stringResource(Res.string.expense_tag_add_new),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            LazyColumn {
                itemsIndexed(screenState.expenseTags) { index, tag ->
                    TagItem(tag = tag, onTagUpdate = onTagUpdate, onTagDelete = onTagDelete)
                }
            }
        }

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.expense_tags_bottom_sheet_button_close)
            )
        }
    }
}

@Composable
fun TagItem(
    tag: ExpenseTag,
    onTagUpdate: (ExpenseTag) -> Unit,
    onTagDelete: (String) -> Unit
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    val isEditing = remember { mutableStateOf(false) }
    val editedName = remember { mutableStateOf(tag.name) }
    val editedColor = remember { mutableStateOf(tag.color) }
    val editedIsEarning = remember { mutableStateOf(tag.isEarning) }
    val uneditedColor = remember { mutableStateOf(tag.color) }
    val uneditedName = remember { mutableStateOf(tag.name) }
    val showColorPicker = remember { mutableStateOf(false) }

    // Delete Confirmation Dialog
    if (showDeleteDialog.value) {
        ExpenseTagDeleteDialog(
            tag = tag,
            onClose = showDeleteDialog,
            onTagDelete = onTagDelete
        )
    }

    if (showColorPicker.value) {
        ExpenseTagColorpickerDialog(
            showColorPicker = showColorPicker,
            editedColor = editedColor
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isEditing.value) {
            Icon(
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                painter = painterResource(resource = Res.drawable.ic_tag_filled),
                contentDescription = null,
                tint = editedColor.value,
            )

            Text(
                modifier = Modifier.weight(1f),
                text = editedName.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = {
                    uneditedColor.value = editedColor.value
                    uneditedName.value = editedName.value
                    isEditing.value = true
                },
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_edit),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            OutlinedIconButton(
                onClick = { showColorPicker.value = true },
                colors = IconButtonColors(
                    containerColor = editedColor.value,
                    disabledContentColor = editedColor.value.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                ),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 12.dp,
                        bottom = 8.dp
                    ),
                    painter = painterResource(resource = Res.drawable.ic_tag_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                )
            }

            TextField(
                modifier = Modifier.weight(1.6f),
                value = editedName.value,
                onValueChange = { editedName.value = it },
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )

            Text(
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                text = if (editedIsEarning.value == true) {
                    stringResource(Res.string.expense_tag_update_earning)
                } else {
                    stringResource(Res.string.expense_tag_update_expense)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Switch(
                checked = editedIsEarning.value ?: false,
                onCheckedChange = {
                    editedIsEarning.value = it
                }
            )

            IconButton(
                onClick = {
                    if (
                        editedColor.value != uneditedColor.value ||
                        editedName.value != uneditedName.value ||
                        editedIsEarning.value != tag.isEarning
                    )
                        onTagUpdate(
                            ExpenseTag(
                                id = tag.id,
                                name = editedName.value,
                                color = editedColor.value,
                                isEarning = editedIsEarning.value,
                                aka = tag.aka,
                            )
                        )
                    isEditing.value = false
                },
//                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_save),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Delete Icon
        IconButton(
            onClick = {
                showDeleteDialog.value = true
            },
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}