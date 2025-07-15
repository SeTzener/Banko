package com.banko.app.ui.screens.details.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.details_note_edit_placeholder
import banko.composeapp.generated.resources.details_note_edit_save
import banko.composeapp.generated.resources.details_note_edit_title_add_note
import banko.composeapp.generated.resources.details_note_edit_title_edit_note
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditNoteBottomSheet(
    initialText: MutableState<String>,
    transactionId: String,
    isEditNote: MutableState<Boolean>,
    onSaveNote: (String, String) -> Unit, // Called when sheet is dismissed with final text
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(initialText.value) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (initialText.value.isEmpty()) {
                    stringResource(Res.string.details_note_edit_title_add_note)
                } else {
                    stringResource(Res.string.details_note_edit_title_edit_note)
                },
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.h4
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text field
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onPrimary,
                focusedIndicatorColor = MaterialTheme.colors.onPrimary,
                unfocusedIndicatorColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f),
                cursorColor = MaterialTheme.colors.onPrimary,
                backgroundColor = MaterialTheme.colors.onSurface,
                placeholderColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f)
            ),
            placeholder = { Text(stringResource(Res.string.details_note_edit_placeholder)) },
            singleLine = false,
            maxLines = 5,
            keyboardActions = KeyboardActions(
                onSend = {
                    isEditNote.value = false
                    onSaveNote(transactionId, text)
                    initialText.value = text
                },
            ),
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Send,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(
            onClick = {
                isEditNote.value = false
                if (text != initialText.value) {
                    onSaveNote(transactionId, text)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.details_note_edit_save))
        }
    }
}