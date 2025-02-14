package com.banko.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import banko.composeapp.generated.resources.expense_tags
import banko.composeapp.generated.resources.expense_tags_title
import banko.composeapp.generated.resources.ic_expense_tags
import banko.composeapp.generated.resources.settings
import com.banko.app.ui.components.SettingsCard
import com.banko.app.ui.screens.settings.bottomsheets.ExpenseTagsBottomSheetContent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(component: SettingsComponent) {
    val viewModel = component.viewModel
    val screenState by viewModel.screenState.collectAsState()
    // Bottom sheet
    var currentBottomSheet by remember { mutableStateOf(BottomSheetType.NONE) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (currentBottomSheet != BottomSheetType.NONE) {
        ModalBottomSheet(
            onDismissRequest = { currentBottomSheet = BottomSheetType.NONE },
            sheetState = sheetState
        ) {
            when (currentBottomSheet) {
                BottomSheetType.EXPENSE_TAGS -> ExpenseTagsBottomSheetContent(
                    screenState = screenState,
                    loadNewTags = { viewModel.getExpenseTags() },
                    onTagUpdate = { viewModel.updateExpenseTag(it) },
                    onTagCreate = { name, color -> viewModel.createExpenseTag(name, color) },
                    onTagDelete = { viewModel.deleteExpenseTag(it) }
                    ) {
                    currentBottomSheet = BottomSheetType.NONE
                }

                BottomSheetType.NONE -> {} // Do nothing
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
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
    NONE
}