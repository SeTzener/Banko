package com.banko.app.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.dialog_transaction_delete_text
import banko.composeapp.generated.resources.dialog_transaction_delete_title
import banko.composeapp.generated.resources.dialog_transaction_delete_cancel
import banko.composeapp.generated.resources.dialog_transaction_delete_delete
import org.jetbrains.compose.resources.stringResource

/***
 * Delete Transaction Dialog
 * Shows a dialog to confirm the deletion of a transaction.
 * It goes back to the previous screen.
 ***/
@Composable
fun TransactionDeleteDialog(
    transactionId: String,
    onDismiss: MutableState<Boolean>,
    onTransactionDelete: (String) -> Unit,
    goBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss.value = false },
        title = {
            Text(
                text = stringResource(Res.string.dialog_transaction_delete_title),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.dialog_transaction_delete_text)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss.value = false
                    onTransactionDelete(transactionId)
                    goBack()
                }
            ) {
                Text(
                    text = stringResource(Res.string.dialog_transaction_delete_delete)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss.value = false }
            ) {
                Text(
                    text = stringResource(Res.string.dialog_transaction_delete_cancel)
                )
            }
        }
    )
}

/***
 * Delete Transaction Dialog
 * Shows a dialog to confirm the deletion of a transaction.
 * It doesn't go back to the previous screen.
 ***/
@Composable
fun TransactionDeleteDialog(
    transactionId: String,
    onDismiss: MutableState<Boolean>,
    onTransactionDelete: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss.value = false },
        title = {
            Text(
                text = stringResource(Res.string.dialog_transaction_delete_title),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.dialog_transaction_delete_text)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss.value = false
                    onTransactionDelete(transactionId)
                }
            ) {
                Text(
                    text = stringResource(Res.string.dialog_transaction_delete_delete)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss.value = false }
            ) {
                Text(
                    text = stringResource(Res.string.dialog_transaction_delete_cancel)
                )
            }
        }
    )
}