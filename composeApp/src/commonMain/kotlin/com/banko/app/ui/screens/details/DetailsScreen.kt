package com.banko.app.ui.screens.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import banko.composeapp.generated.resources.details
import banko.composeapp.generated.resources.details_booking_date
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
import banko.composeapp.generated.resources.details_remittance_information
import banko.composeapp.generated.resources.details_value_date
import banko.composeapp.generated.resources.expense_tag_no_tag
import banko.composeapp.generated.resources.ic_arrow_drop_down
import banko.composeapp.generated.resources.ic_calendar_month
import banko.composeapp.generated.resources.ic_quill
import banko.composeapp.generated.resources.ic_save
import banko.composeapp.generated.resources.ic_tag
import banko.composeapp.generated.resources.ic_tag_filled
import com.banko.app.ui.models.ExpenseTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DetailsScreen(component: DetailsComponent) {
    var transaction by remember { mutableStateOf(component.transaction) }
    var oldTag by remember { mutableStateOf(transaction.expenseTag?.id) }
    val viewModel = koinViewModel<DetailsScreenViewModel>()
    val screenState by viewModel.screenState.collectAsState()

    val isEditing by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateOf(false) }
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
                modifier = Modifier.align(Alignment.Bottom)
                    .padding(top = 24.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.End)
                )
                {
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
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    onValueChange = {},
                    value = transaction.remittanceInformationUnstructuredArray.joinToString(" "),
                    supportingText = {
                        Text(
                            text = stringResource(Res.string.details_remittance_information)
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
                                    readOnly = !isEditing,
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
                                    readOnly = !isEditing,
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
                            IconButton(
                                onClick = {
                                    viewModel.getExpenseTags()
                                    expanded.value = true
                                },
                                content = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_arrow_drop_down),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    ExpenseTagDropdown(
                                        expanded = expanded,
                                        expenseTags = screenState.expenseTags,
                                        onTagSelected = { tag ->
                                            transaction = transaction.copy(expenseTag = tag)
                                        }
                                    )
                                }
                            )
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
                            modifier = Modifier.align(Alignment.Bottom),
                            onClick = {
                                viewModel.assignExpenseTag(transaction.id, transaction.expenseTag?.id)
                                oldTag = transaction.expenseTag?.id
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_save),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { component.goBack() }
        ) {
            Text(stringResource(Res.string.details_button_back))
        }
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
            }
        )
        expenseTags.forEach { tag ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_tag_filled),
                        contentDescription = null,
                        tint = tag.color
                    )
                },
                text = {
                    Text(
                        text = tag.name,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    onTagSelected(tag)
                    expanded.value = false
                }
            )
        }
    }
}