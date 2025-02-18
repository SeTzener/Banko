package com.banko.app.ui.screens.settings.bottomsheets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.expense_tag_delete_no
import banko.composeapp.generated.resources.expense_tag_delete_text
import banko.composeapp.generated.resources.expense_tag_delete_title
import banko.composeapp.generated.resources.expense_tag_delete_yes
import com.banko.app.ui.models.ExpenseTag
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseTagDeleteDialog(
    tag: ExpenseTag,
    onClose: MutableState<Boolean>,
    onTagDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onClose.value = false }, // Close dialog on outside click
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
                    onClose.value = false // Close the dialog
                    onTagDelete(tag.id) // Trigger delete action
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
                onClick = { onClose.value = false } // Close the dialog
            ) {
                Text(
                    text = stringResource(Res.string.expense_tag_delete_no)
                )
            }
        }
    )
}