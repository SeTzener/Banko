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
import banko.composeapp.generated.resources.details_expense_tag_empty
import banko.composeapp.generated.resources.ic_tag
import banko.composeapp.generated.resources.ic_tag_filled
import com.banko.app.ui.models.ExpenseTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpenseTagDropdown(
    expanded: MutableState<Boolean>,
    expenseTags: List<ExpenseTag>,
    onTagSelected: (ExpenseTag?) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.background(MaterialTheme.colorScheme.onSurface),
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_tag),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.details_expense_tag_empty),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                onTagSelected(null)
                expanded.value = false
            })
        expenseTags.forEach { tag ->
            DropdownMenuItem(leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_tag_filled),
                    contentDescription = null,
                    tint = tag.color
                )
            }, text = {
                Text(
                    text = tag.name, color = MaterialTheme.colorScheme.primary
                )
            }, onClick = {
                onTagSelected(tag)
                expanded.value = false
            })
        }
    }
}