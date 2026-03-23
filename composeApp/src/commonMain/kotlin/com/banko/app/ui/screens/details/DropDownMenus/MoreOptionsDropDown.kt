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
import banko.composeapp.generated.resources.details_button_add_note
import banko.composeapp.generated.resources.ic_delete
import banko.composeapp.generated.resources.ic_edit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreOptionsDropDown(
    expanded: MutableState<Boolean>,
    isAddNote: MutableState<Boolean>,
    isDeleteTransaction: MutableState<Boolean>
){
    DropdownMenu(
        modifier = Modifier.background(MaterialTheme.colorScheme.onSurface),
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.details_button_add_note),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                expanded.value = false
                isAddNote.value = true
            }
        )

        DropdownMenuItem(
            leadingIcon =  {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            },
            onClick = {
                expanded.value = false
                isDeleteTransaction.value = true
            }
        )
    }
}