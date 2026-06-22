// TODO: Remove this file (temporary debugging aid)
package com.banko.app.ui.components.dialogs

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.error_details_close
import banko.composeapp.generated.resources.error_details_no_message
import banko.composeapp.generated.resources.error_details_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorDetailDialog(
    fullError: String?,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(Res.string.error_details_title))
        },
        text = {
            SelectionContainer {
                Text(
                    text = fullError ?: stringResource(Res.string.error_details_no_message),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.error_details_close))
            }
        }
    )
}
