package com.banko.app.ui.screens.settings.bottomsheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.expense_tag_delete_no
import banko.composeapp.generated.resources.expense_tag_delete_text
import banko.composeapp.generated.resources.expense_tag_delete_title
import banko.composeapp.generated.resources.expense_tag_delete_yes
import banko.composeapp.generated.resources.expense_tags_bottom_sheet_button_close
import banko.composeapp.generated.resources.expense_tags_title
import banko.composeapp.generated.resources.ic_delete
import banko.composeapp.generated.resources.ic_edit
import banko.composeapp.generated.resources.ic_tag
import com.banko.app.api.dto.bankoApi.ExpenseTag
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseTagsBottomSheetContent(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Take up all available space
        ) {
            Text(
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                text = stringResource(Res.string.expense_tags_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            // Example list of tags
            val tags = listOf(
                ExpenseTag(
                    id = "123456",
                    name = "Grocery",
                    color = 0xFF003366,
                    aka = null
                ),
                ExpenseTag(
                    id = "123457",
                    name = "Bills",
                    color = 0xFFFFBF00,
                    aka = null
                ),
                ExpenseTag(
                    id = "123458",
                    name = "Rent",
                    color = 0xFF800080,
                    aka = null
                ),
                ExpenseTag(
                    id = "123459",
                    name = "Cigarettes",
                    color = 0xFF008080,
                    aka = null
                ),
            )
            tags.forEach { tag ->
                val onTagUpdate = { updatedTag: ExpenseTag -> } // TODO() Add the edit call here
                val onTagDelete = { deletedTag: ExpenseTag -> } // TODO() Add the delete call here
                TagItem(tag = tag, onTagUpdate = onTagUpdate, onTagDelete = onTagDelete)
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

@OptIn(InternalResourceApi::class)
@Composable
fun TagItem(tag: ExpenseTag, onTagUpdate: (ExpenseTag) -> Unit, onTagDelete: (ExpenseTag) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(tag.name) }
    var editedColor by remember { mutableStateOf(tag.color) }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false }, // Close dialog on outside click
            title = {
                Text(
                    text = stringResource(Res.string.expense_tag_delete_title),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.expense_tag_delete_text),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false // Close the dialog
                        onTagDelete(tag) // Trigger delete action
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.expense_tag_delete_yes),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false } // Close the dialog
                ) {
                    Text(
                        text = stringResource(Res.string.expense_tag_delete_no)
                    )
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tag Icon
        if (!isEditing) {
            Icon(
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                painter = painterResource(resource = Res.drawable.ic_tag),
                contentDescription = null,
                tint = Color(tag.color),
            )
        } else {
            IconButton(
                onClick = {  },
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                    painter = painterResource(resource = Res.drawable.ic_tag),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                )
            }
        }
        // Tag Name
        if (!isEditing) {
            Text(
                modifier = Modifier.weight(1f),
                text = editedName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            TextField(
                modifier = Modifier.weight(1f),
                value = editedName,
                onValueChange = { editedName = it },
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
                keyboardActions = KeyboardActions(
                    onDone = {
                        isEditing = false
                        onTagUpdate// TODO(): Update tag name
                    }
                )
            )
        }

        IconButton(
            onClick = { isEditing = !isEditing },
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_edit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        // Delete Icon
        IconButton(
            onClick = {
                showDeleteDialog = true
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