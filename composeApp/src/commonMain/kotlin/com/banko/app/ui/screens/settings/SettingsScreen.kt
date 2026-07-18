package com.banko.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.account_balance
import banko.composeapp.generated.resources.add_new_bank
import banko.composeapp.generated.resources.add_new_bank_description
import banko.composeapp.generated.resources.banks
import banko.composeapp.generated.resources.currency
import banko.composeapp.generated.resources.expense_tags
import banko.composeapp.generated.resources.expense_tags_title
import banko.composeapp.generated.resources.general
import banko.composeapp.generated.resources.ic_arrow_right
import banko.composeapp.generated.resources.ic_currency
import banko.composeapp.generated.resources.ic_expense_tags
import banko.composeapp.generated.resources.linked_banks
import banko.composeapp.generated.resources.profile
import banko.composeapp.generated.resources.settings
import com.banko.app.domain.model.currencyDisplayForCode
import com.banko.app.ui.components.SettingsCard
import com.banko.app.ui.screens.settings.bottomsheets.ExpenseTagsBottomSheetContent
import com.banko.app.ui.screens.settings.bottomsheets.LinkedBanksBottomSheetContent
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun SettingsScreen(component: SettingsComponent) {
    val viewModel = koinViewModel<SettingsScreenViewModel>()
    val screenState by viewModel.screenState.collectAsState()
    var currentBottomSheet by remember { mutableStateOf(BottomSheetType.NONE) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (currentBottomSheet == BottomSheetType.EXPENSE_TAGS) {
        ModalBottomSheet(
            onDismissRequest = { currentBottomSheet = BottomSheetType.NONE },
            sheetState = sheetState
        ) {
            ExpenseTagsBottomSheetContent(
                screenState = screenState,
                loadNewTags = { viewModel.loadExpenseTags() },
                onTagUpdate = { viewModel.updateExpenseTag(it) },
                onTagCreate = { name, color, isEarning -> viewModel.createExpenseTag(name, color, isEarning) },
                onTagDelete = { viewModel.deleteExpenseTag(it) },
                clearError = { viewModel.clearError() },
                onClose = { currentBottomSheet = BottomSheetType.NONE }
            )
        }
    }

    if (currentBottomSheet == BottomSheetType.LINKED_BANKS) {
        ModalBottomSheet(
            onDismissRequest = { currentBottomSheet = BottomSheetType.NONE },
            sheetState = sheetState
        ) {
            LinkedBanksBottomSheetContent(
                screenState = screenState,
                onReAuthorize = { institutionId -> component.onReAuthorize(institutionId) },
                onClose = { currentBottomSheet = BottomSheetType.NONE },
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .padding(16.dp),
            text = stringResource(Res.string.settings),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium
        )
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
        ) {
            Row {
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.profile),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Row {
                SettingsCard(
                    icon = Res.drawable.ic_expense_tags,
                    title = Res.string.profile,
                    description = Res.string.profile,
                    onClick = component.onNavigateToProfile,
                )
            }
            Row {
                Text(
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                    text = stringResource(Res.string.general),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currencyDropdownExpanded = true }
                        .padding(top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 24.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.ic_currency),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.currency),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currencyDisplayForCode(screenState.selectedCurrency.code),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 4.dp),
                                painter = painterResource(Res.drawable.ic_arrow_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier.heightIn(max = 280.dp),
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false }
                        ) {
                            screenState.availableCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${currency.symbol}  ${currency.code} — ${currency.name}",
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    },
                                    onClick = {
                                        viewModel.setCurrency(currency.code)
                                        currencyDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        RadioButton(
                                            selected = currency.code == screenState.selectedCurrency.code,
                                            onClick = null,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
            Row {
                Text(
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                    text = stringResource(Res.string.banks),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Row {
                SettingsCard(
                    icon = Res.drawable.account_balance,
                    title = Res.string.add_new_bank,
                    description = Res.string.add_new_bank_description,
                    onClick = component.onNavigateToBankLinking,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                SettingsCard(
                    icon = Res.drawable.account_balance,
                    title = Res.string.linked_banks,
                    description = Res.string.linked_banks,
                    onClick = { currentBottomSheet = BottomSheetType.LINKED_BANKS },
                )
            }
            Row {
                Text(
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                    text = stringResource(Res.string.expense_tags_title),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Row {
                SettingsCard(
                    icon = Res.drawable.ic_expense_tags,
                    title = Res.string.expense_tags,
                    description = Res.string.expense_tags,
                    onClick = { currentBottomSheet = BottomSheetType.EXPENSE_TAGS }
                )
            }
        }
    }
}

enum class BottomSheetType {
    EXPENSE_TAGS,
    LINKED_BANKS,
    NONE
}
