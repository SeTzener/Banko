// TODO: Remove this file (temporary debugging aid)
package com.banko.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.banko.app.ui.components.dialogs.ErrorDetailDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ErrorSnackbarHost(
    hostState: SnackbarHostState,
    rawError: String?,
    modifier: Modifier = Modifier,
) {
    var showErrorDetailDialog by remember { mutableStateOf(false) }
    var dialogFullError by remember { mutableStateOf("") }

    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        Box(
            modifier = Modifier.combinedClickable(
                onClick = { },
                onLongClick = {
                    dialogFullError = rawError ?: data.visuals.message
                    showErrorDetailDialog = true
                }
            )
        ) {
            Snackbar(snackbarData = data)
        }
    }

    if (showErrorDetailDialog) {
        ErrorDetailDialog(
            fullError = dialogFullError,
            onDismiss = { showErrorDetailDialog = false },
        )
    }
}
