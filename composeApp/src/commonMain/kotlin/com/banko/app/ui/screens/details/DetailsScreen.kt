package com.banko.app.ui.screens.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.account_balance
import banko.composeapp.generated.resources.details
import banko.composeapp.generated.resources.details_booking_date
import banko.composeapp.generated.resources.details_button_add_note
import banko.composeapp.generated.resources.details_button_back
import banko.composeapp.generated.resources.details_creditor_bban
import banko.composeapp.generated.resources.details_creditor_iban
import banko.composeapp.generated.resources.details_creditor_name
import banko.composeapp.generated.resources.details_creditor_name_missing
import banko.composeapp.generated.resources.details_debtor_bban
import banko.composeapp.generated.resources.details_debtor_iban
import banko.composeapp.generated.resources.details_debtor_name
import banko.composeapp.generated.resources.details_debtor_name_missing
import banko.composeapp.generated.resources.details_expense_tag
import banko.composeapp.generated.resources.details_expense_tag_empty
import banko.composeapp.generated.resources.details_note
import banko.composeapp.generated.resources.details_remittance_information
import banko.composeapp.generated.resources.details_value_date
import banko.composeapp.generated.resources.expense_tag_no_tag
import banko.composeapp.generated.resources.generic_button_delete
import banko.composeapp.generated.resources.generic_button_edit
import banko.composeapp.generated.resources.ic_arrow_drop_down
import banko.composeapp.generated.resources.ic_calendar_month
import banko.composeapp.generated.resources.ic_delete
import banko.composeapp.generated.resources.ic_edit
import banko.composeapp.generated.resources.ic_more
import banko.composeapp.generated.resources.ic_quill
import banko.composeapp.generated.resources.ic_save
import banko.composeapp.generated.resources.ic_tag
import banko.composeapp.generated.resources.ic_tag_filled
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.models.Transaction
import com.banko.app.ui.screens.details.bottomsheets.EditNoteBottomSheet
import com.banko.app.ui.screens.details.bottomsheets.dialogs.NoteDeleteDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DetailsScreen(component: DetailsComponent) {
    val viewModel = koinViewModel<DetailsScreenViewModel>()
    val screenState by viewModel.screenState.collectAsState()

    DetailsScreen(
        transactions = component.transaction,
        expenseTags = screenState.expenseTags,
        saveNote = { note: String, id: String -> viewModel.saveNote(id, note) },
        getExpenseTags = { viewModel.getExpenseTags() },
        assignExpenseTag = { id: String, tagId: String? -> viewModel.assignExpenseTag(id, tagId) },
        goBack = { component.goBack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    transactions: Transaction,
    expenseTags: List<ExpenseTag>,
    saveNote: (String, String) -> Unit,
    assignExpenseTag: (String, String?) -> Unit,
    getExpenseTags: () -> Unit,
    goBack: () -> Unit
) {
    var transaction by remember { mutableStateOf(transactions) }
    val transactionNote = remember { mutableStateOf(transaction.note ?: "") }
    var oldTag by remember { mutableStateOf(transaction.expenseTag?.id) }
    val isEditing by remember { mutableStateOf(false) }
    val isEditNote = remember { mutableStateOf(false) }
    val isDeleteNote = remember { mutableStateOf(false) }
    val tagMenuExpanded = remember { mutableStateOf(false) }
    val noteMenuExpanded = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isEditNote.value) {
        ModalBottomSheet(
            onDismissRequest = { isEditNote.value = false },
            sheetState = sheetState
        ) {
            EditNoteBottomSheet(
                initialText = transactionNote,
                transactionId = transaction.id,
                onSaveNote = saveNote,
                isEditNote = isEditNote
            )
        }
    }

    if(isDeleteNote.value) {
        NoteDeleteDialog(
            onDismiss = isDeleteNote,
            textToDelete = transactionNote,
            transactionId = transaction.id,
            onNoteDelete = saveNote
        )
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            Text(
                modifier = Modifier.padding(16.dp).weight(1f),
                text = stringResource(Res.string.details),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium
            )
            Column(
                modifier = Modifier.align(Alignment.Bottom).padding(top = 24.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = transaction.amount.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        modifier = Modifier.align(Alignment.Bottom),
                        text = transaction.currency,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    if (transaction.expenseTag != null) {
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            painter = painterResource(Res.drawable.ic_tag_filled),
                            contentDescription = null,
                            tint = transaction.expenseTag!!.color
                        )
                    }
                    Text(
                        text = transaction.expenseTag?.name
                            ?: stringResource(Res.string.expense_tag_no_tag),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            // Remittance Information
            item {
                TextField(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxWidth(),
                    onValueChange = {},
                    value = transaction.remittanceInformationUnstructuredArray.joinToString(" "),
                    supportingText = {
                        Text(
                            text = stringResource(Res.string.details_remittance_information)
                        )
                    },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.account_balance),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
            }

            // Booking Date and Value Date
            item {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        onValueChange = {},
                        value = transaction.bookingDate.date.toString(),
                        readOnly = true,
                        supportingText = {
                            Text(
                                text = stringResource(Res.string.details_booking_date)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_calendar_month),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
                    TextField(
                        modifier = Modifier.weight(1f),
                        onValueChange = {},
                        value = transaction.valueDate.date.toString(),
                        readOnly = true,
                        supportingText = {
                            Text(
                                text = stringResource(Res.string.details_value_date)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_calendar_month),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
                }
            }

            // Creditor Informations
            if (transaction.creditorAccount != null) {
                item {
                    Card(
                        modifier = Modifier.padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, Color.LightGray),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                            ) {
                                TextField(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    onValueChange = {},
                                    value = transaction.creditorName
                                        ?: stringResource(Res.string.details_creditor_name_missing),
                                    readOnly = true,
                                    supportingText = {
                                        Text(
                                            text = stringResource(Res.string.details_creditor_name)
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                        focusedTextColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row {
                                    TextField(
                                        modifier = Modifier.weight(1f),
                                        onValueChange = {},
                                        value = transaction.creditorAccount!!.iban,
                                        readOnly = true,
                                        supportingText = {
                                            Text(
                                                text = stringResource(Res.string.details_creditor_iban)
                                            )
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        colors = TextFieldDefaults.colors(
                                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                            focusedTextColor = MaterialTheme.colorScheme.primary,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        )
                                    )
                                }
                                Row {
                                    TextField(
                                        modifier = Modifier.weight(1f),
                                        onValueChange = {},
                                        value = transaction.creditorAccount!!.bban,
                                        readOnly = true,
                                        supportingText = {
                                            Text(
                                                text = stringResource(Res.string.details_creditor_bban)
                                            )
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        colors = TextFieldDefaults.colors(
                                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                            focusedTextColor = MaterialTheme.colorScheme.primary,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Debtor Informations
            if (transaction.debtorAccount != null) {
                item {
                    Card(
                        modifier = Modifier.padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, Color.LightGray),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                            ) {
                                TextField(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    onValueChange = {},
                                    value = transaction.debtorName
                                        ?: stringResource(Res.string.details_debtor_name_missing),
                                    readOnly = true,
                                    supportingText = {
                                        Text(
                                            text = stringResource(Res.string.details_debtor_name)
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                        focusedTextColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row {
                                    TextField(
                                        modifier = Modifier.weight(1f),
                                        onValueChange = {},
                                        value = transaction.debtorAccount!!.iban,
                                        readOnly = true,
                                        supportingText = {
                                            Text(
                                                text = stringResource(Res.string.details_debtor_iban)
                                            )
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        colors = TextFieldDefaults.colors(
                                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                            focusedTextColor = MaterialTheme.colorScheme.primary,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        )
                                    )
                                }
                                Row {
                                    TextField(
                                        modifier = Modifier.weight(1f),
                                        onValueChange = {},
                                        value = transaction.debtorAccount!!.bban,
                                        readOnly = true,
                                        supportingText = {
                                            Text(
                                                text = stringResource(Res.string.details_debtor_bban)
                                            )
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        colors = TextFieldDefaults.colors(
                                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                            focusedTextColor = MaterialTheme.colorScheme.primary,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expense Tag
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier.padding(top = 12.dp, start = 16.dp),
                        onValueChange = {},
                        value = transaction.expenseTag?.name
                            ?: stringResource(Res.string.details_expense_tag),
                        readOnly = true,
                        supportingText = {
                            Text(
                                text = stringResource(Res.string.details_expense_tag)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = if (transaction.expenseTag != null) {
                                    painterResource(Res.drawable.ic_tag_filled)
                                } else {
                                    painterResource(Res.drawable.ic_tag)
                                },
                                contentDescription = null,
                                tint = transaction.expenseTag?.color
                                    ?: MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                getExpenseTags()
                                tagMenuExpanded.value = true
                            }, content = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_arrow_drop_down),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                ExpenseTagDropdown(
                                    expanded = tagMenuExpanded,
                                    expenseTags = expenseTags,
                                    onTagSelected = { tag ->
                                        transaction = transaction.copy(expenseTag = tag)
                                    })
                            })
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
                    if (oldTag != transaction.expenseTag?.id) {
                        IconButton(
                            modifier = Modifier.align(Alignment.Bottom), onClick = {
                                assignExpenseTag(
                                    transaction.id, transaction.expenseTag?.id
                                )
                                oldTag = transaction.expenseTag?.id
                            }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_save),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Note
            item {
                if (transactionNote.value.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillParentMaxWidth().padding(top = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, Color.LightGray),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 16.dp, end = 36.dp, bottom = 16.dp),
                                onValueChange = {},
                                value = transactionNote.value,
                                supportingText = {
                                    Text(
                                        text = stringResource(Res.string.details_note)
                                    )
                                },
                                readOnly = !isEditing,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_quill),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                )
                            )

                            IconButton(
                                modifier = Modifier.padding(end = 4.dp).align(Alignment.TopEnd),
                                onClick = {
                                    noteMenuExpanded.value = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_more),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                NoteDropDown(noteMenuExpanded, isEditNote, isDeleteNote)
                            }
                        }
                    }
                }
            }
        }

        // Buttons
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            if (transactionNote.value.isEmpty()) {
                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        isEditNote.value = true
                    }
                ) {
                    Text(stringResource(Res.string.details_button_add_note))
                }
            }
            Button(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = { goBack() }
            ) {
                Text(stringResource(Res.string.details_button_back))
            }
        }
    }
}

@Composable
private fun NoteDropDown(
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

@Composable
private fun ExpenseTagDropdown(
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