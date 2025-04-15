package com.banko.app.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import kotlin.random.Random

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    val viewModel = koinViewModel<HomeScreenViewModel>()
    val state = viewModel.state.collectAsState()

    HomeScreen(
        state = state,
        navigateToDetails = component::navigateToDetails,
        loadMore = { viewModel.handleEvent(event = TransactionsEvent.LoadMore) },
        onRefresh = { viewModel.handleEvent(event = TransactionsEvent.Refresh) },
        clearError = { viewModel.handleEvent(TransactionsEvent.ErrorShown(it)) }
    )
}

@Composable
fun HomeScreen(
    state: State<HomeScreenState>,
    navigateToDetails: (ModelTransaction) -> Unit,
    loadMore: () -> Unit,
    onRefresh: () -> Unit,
    clearError: (String) -> Unit
) {
    val liststate = rememberLazyListState()
    val snackbarHoststate = remember { SnackbarHostState() }

    LaunchedEffect(state.value.error) {
        state.value.error?.let { error ->
            snackbarHoststate.showSnackbar(error)
            clearError(error)
        }
    }

    LaunchedEffect(liststate) {
        snapshotFlow { liststate.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (state.value.isLoading || state.value.endReached) return@collect

                val lastVisibleItem = visibleItems.lastOrNull()
                if (lastVisibleItem != null &&
                    lastVisibleItem.index >= liststate.layoutInfo.totalItemsCount - 5
                ) {
                    loadMore()
                }
            }
    }

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
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHoststate) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Your existing content here

                LazyTransactionList(
                    isLoading = state.value.isLoading,
                    isRefreshing = state.value.isRefreshing,
                    listState = liststate,
                    transactions = state.value.transactions,
                    navigateToDetails = navigateToDetails,
                    onRefresh = onRefresh,
                )
            }
        }
    }
}


@Composable
private fun SwipableTransactionRow(transaction: Transaction, onDetailsClick: () -> Unit) {
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

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private fun LazyTransactionList(
    isLoading: Boolean,
    isRefreshing: Boolean,
    transactions: List<ModelTransaction>,
    listState: LazyListState,
    navigateToDetails: (ModelTransaction) -> Unit,
    onRefresh: () -> Unit,
) {
    val groupedTransactions = transactions.groupBy { it.bookingDate.date }

    LoadingProgressIndicator(isLoading = isLoading)
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(state = listState) {
            groupedTransactions.forEach { (date, transactionsByDate) ->
                stickyHeader {
                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.onSurface)
                            .padding(12.dp),
                        text = date.toString(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                items(
                    items = transactionsByDate,
                    key = { transaction -> transaction.id }
                ) { transaction ->
                    SwipableTransactionRow(
                        transaction = transaction,
                        onDetailsClick = { navigateToDetails(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingProgressIndicator(
    isLoading: Boolean,
) {
    if (!isLoading) return

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(4.dp)) {
        val infiniteTransition = rememberInfiniteTransition()
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val gradientWidth = width * 0.3f

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.8f),
                            primaryColor,
                            primaryColor.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        start = Offset(progress * width * 1.5f - gradientWidth, 0f),
                        end = Offset(progress * width * 1.5f + gradientWidth, 0f)
                    ),
                    size = size
                )
            }
        }
    }
}