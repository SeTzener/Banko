package com.banko.app.ui.screens.details.DropDownMenus

import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.generic_button_delete
import banko.composeapp.generated.resources.generic_button_edit
import banko.composeapp.generated.resources.ic_delete
import banko.composeapp.generated.resources.ic_edit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NoteDropDown(
    expanded: MutableState<Boolean>,
    isEditNote: MutableState<Boolean>,
    isDeleteNote: MutableState<Boolean>
) {
    DropdownMenu(
        modifier = Modifier.background(MaterialTheme.colorScheme.onSurface),
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.generic_button_edit),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                expanded.value = false
                isEditNote.value = true
            }
        )

        DropdownMenuItem(
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.generic_button_delete),
                    color = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                expanded.value = false
                isDeleteNote.value = true
            }
        )
    }
}