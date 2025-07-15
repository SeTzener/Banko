package com.banko.app.ui.screens.details.bottomsheets.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.details_note_delete_no
import banko.composeapp.generated.resources.details_note_delete_text
import banko.composeapp.generated.resources.details_note_delete_title
import banko.composeapp.generated.resources.details_note_delete_yes
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteDeleteDialog(
    transactionId: String,
    onDismiss: MutableState<Boolean>,
    onNoteDelete: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss.value = false }, // Close dialog on outside click
        title = {
            Text(
                text = stringResource(Res.string.details_note_delete_title),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.details_note_delete_text),
                color = MaterialTheme.colorScheme.primary
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss.value = false // Close the dialog
                    onNoteDelete(transactionId, "") // Trigger delete action
                }
            ) {
                Text(
                    text = stringResource(Res.string.details_note_delete_yes),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss.value = false } // Close the dialog
            ) {
                Text(
                    text = stringResource(Res.string.details_note_delete_no)
                )
            }
        }
    )
}