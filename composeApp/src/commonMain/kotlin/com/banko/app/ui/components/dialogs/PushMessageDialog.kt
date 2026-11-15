package com.banko.app.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.push_dialog_dismiss
import banko.composeapp.generated.resources.push_dialog_manage
import banko.composeapp.generated.resources.push_eua_expired_message
import banko.composeapp.generated.resources.push_eua_expired_title
import banko.composeapp.generated.resources.push_eua_warning_message
import banko.composeapp.generated.resources.push_eua_warning_title
import com.banko.app.push.PushMessage
import com.banko.app.push.PushMessageType
import org.jetbrains.compose.resources.stringResource

/**
 * Explains a backend push inside the app instead of leaving the user with a
 * notification they cannot interpret.
 */
@Composable
fun PushMessageDialog(
    message: PushMessage,
    onDismiss: () -> Unit,
    onManageConnections: () -> Unit,
) {
    val title = when (message.type) {
        PushMessageType.EuaExpiryWarning -> stringResource(Res.string.push_eua_warning_title)
        PushMessageType.EuaExpiryExpired -> stringResource(Res.string.push_eua_expired_title)
    }
    val body = when (message.type) {
        PushMessageType.EuaExpiryWarning -> stringResource(Res.string.push_eua_warning_message)
        PushMessageType.EuaExpiryExpired -> stringResource(Res.string.push_eua_expired_message)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onManageConnections) {
                Text(stringResource(Res.string.push_dialog_manage))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.push_dialog_dismiss))
            }
        },
    )
}
