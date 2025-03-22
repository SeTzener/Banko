package com.banko.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.flatMap
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemContentType
import app.cash.paging.compose.itemKey
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.account_balance
import banko.composeapp.generated.resources.app_name
import banko.composeapp.generated.resources.currency_nok
import banko.composeapp.generated.resources.daily_budget
import banko.composeapp.generated.resources.details
import banko.composeapp.generated.resources.monthly_budget
import banko.composeapp.generated.resources.monthly_income
import banko.composeapp.generated.resources.payments
import com.banko.app.ModelTransaction
import com.banko.app.ui.components.CircularIndicator
import com.banko.app.ui.components.ExpandableCard
import com.banko.app.ui.components.ExpenseTag
import com.banko.app.ui.components.TextWithIcon
import com.banko.app.ui.models.Transaction
import com.banko.app.ui.models.categories
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class, KoinExperimentalAPI::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    val viewModel = koinViewModel<HomeScreenViewModel>()
    val screenState by viewModel.screenState.collectAsState()
    val listState = rememberLazyListState()
    val transactions = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExpandableCard(
            isExpanded = true,
            topContent = { TopContent() },
            expandedContent = {
                CircularIndicator(
                    dailyBudget = Random.nextInt(100),
                    currency = stringResource(Res.string.currency_nok),
                    dailyBudgetText = stringResource(Res.string.daily_budget),
                    monthlyBudgetText = stringResource(Res.string.monthly_budget),
                    monthlyBudget = 123456789,
                    categories = categories.take(Random.nextInt(categories.size - 1))
                )
            }
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardColors(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(0.dp, Color.LightGray),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 8.dp
                    ).fillMaxWidth()
                ) {
                    Column {
                        TextWithIcon(
                            text = stringResource(resource = Res.string.monthly_income),
                            iconResId = Res.drawable.payments,
                            textColor = MaterialTheme.colorScheme.primary,
                            iconPadding = 8.dp
                        )
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.align(Alignment.End),
                            text = "800 Nok"
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(
                        top = 8.dp,
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp
                    ).fillMaxWidth()
                ) {
                    Column {
                        TextWithIcon(
                            text = stringResource(Res.string.monthly_budget),
                            iconResId = Res.drawable.account_balance,
                            textColor = MaterialTheme.colorScheme.primary,
                            iconPadding = 8.dp
                        )
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.align(Alignment.End),
                            text = "40.000 Nok"
                        )
                    }
                }
            }
        }

        if (transactions.itemCount == 0) {
            Box(
                modifier = Modifier.fillMaxSize().padding(start = 5.dp, end = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(35.dp),
                    strokeWidth = 5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        LazyColumn(
            state = listState
        ) {
            items(
                key = transactions.itemKey { item ->
                    when (item) {
                        is TransactionPagingData.Item -> "Item_${item.modelTransaction.id}"
                        is TransactionPagingData.Separator -> "Separator_${item.date}"
                    }
                },
                contentType = transactions.itemContentType { item ->
                    when (item) {
                        is TransactionPagingData.Item -> 0
                        is TransactionPagingData.Separator -> 1
                    }
                },
                count = transactions.itemCount
            ) { index ->
                when (val item = transactions[index]) {
                    is TransactionPagingData.Separator ->
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.onSurface)
                                .padding(12.dp),
                            text = item.date.toString(),
                            color = MaterialTheme.colorScheme.secondary
                        )

                    is TransactionPagingData.Item ->
                        SwipableTransactionRow(
                            transaction = item.modelTransaction,
                            onDetailsClick = {
                                component.onEvent(
                                    HomeEvent.ButtonClick,
                                    item.modelTransaction
                                )
                            }
                        )

                    null -> Unit
                }
            }
            if (screenState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SwipableTransactionRow(transaction: Transaction, onDetailsClick: () -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    val buttonWidth = 85.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        TextButton(
            onClick = onDetailsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
                .width(85.dp)
                .height(32.dp)
        ) {
            Text(
                text = stringResource(Res.string.details),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Main Transaction Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume() // Consume gesture event
                            offsetX = (offsetX + dragAmount).coerceIn(-buttonWidth.value, 0f)
                        },
                        onDragEnd = {
                            // Snap to position
                            offsetX =
                                if (offsetX < -buttonWidth.value / 2) -buttonWidth.value else 0f
                        }
                    )
                }
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(7f)
                    .align(Alignment.CenterVertically)
                    .padding(start = 12.dp, end = 7.dp)
            ) {
                Text(
                    text = transaction.remittanceInformationUnstructured,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier
                    .weight(3f)
                    .align(Alignment.CenterVertically)
                    .padding(end = 5.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = "${transaction.amount} ${transaction.currency}",
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                ExpenseTag(transaction.expenseTag)
            }
        }
    }
}

@Composable
private fun TopContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}