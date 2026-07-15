package com.banko.app.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.app_name
import banko.composeapp.generated.resources.details
import banko.composeapp.generated.resources.ic_delete
import banko.composeapp.generated.resources.ic_tag
import banko.composeapp.generated.resources.ic_tag_filled
import com.banko.app.ModelTransaction
import com.banko.app.ui.components.BankLogo
import com.banko.app.ui.components.CircularIndicator
import com.banko.app.ui.components.ErrorSnackbarHost
import com.banko.app.ui.components.ExpenseTag
import com.banko.app.ui.models.Transaction
import com.banko.app.ui.components.dialogs.TransactionDeleteDialog
import com.banko.app.domain.model.currencyDisplayForCode
import com.banko.app.ui.utils.toUserMessage
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import kotlin.math.roundToInt

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    val viewModel = koinViewModel<HomeScreenViewModel>()
    val transactionListState by viewModel.transactionListState.collectAsState()
    val filteredTransactionListState by viewModel.filteredTransactionListState.collectAsState()
    val timespanState by viewModel.timespanState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val isUncategorizedSelected by viewModel.isUncategorizedSelected.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    HomeScreen(
        transactionListState = transactionListState,
        filteredTransactionListState = filteredTransactionListState,
        timespanState = timespanState,
        uiState = uiState,
        selectedCurrency = selectedCurrency,
        selectedCategoryId = selectedCategoryId,
        isUncategorizedSelected = isUncategorizedSelected,
        navigateToDetails = component::navigateToDetails,
        onTimespanSelected = { viewModel.handleEvent(TransactionsEvent.SelectTimespan(it)) },
        onRefresh = { viewModel.handleEvent(event = TransactionsEvent.Refresh) },
        clearError = { viewModel.clearError() },
        onDeleteTransaction = { viewModel.handleEvent(TransactionsEvent.DeleteTransaction(it)) },
        onToggleView = { viewModel.handleEvent(TransactionsEvent.ToggleTimespanView) },
        onLoadMore = { viewModel.handleEvent(TransactionsEvent.LoadMore) },
        onCategoryClick = { viewModel.handleEvent(TransactionsEvent.SelectTag(it)) },
    )
}

@Composable
fun HomeScreen(
    transactionListState: TransactionListState,
    filteredTransactionListState: TransactionListState,
    timespanState: TimespanState,
    uiState: UiState,
    selectedCurrency: String,
    selectedCategoryId: String?,
    isUncategorizedSelected: Boolean,
    navigateToDetails: (ModelTransaction) -> Unit,
    onTimespanSelected: (TimespanSelection) -> Unit,
    onRefresh: () -> Unit,
    clearError: () -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onToggleView: () -> Unit,
    onLoadMore: () -> Unit,
    onCategoryClick: (String?) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var dragOffset by remember { mutableStateOf(0f) }
    val swipeThreshold = 50f
    val currentTimespanState by rememberUpdatedState(timespanState)

    val errorMessage = uiState.error?.let { it.type.toUserMessage() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            clearError()
        }
    }

    var foregroundHeightPx by remember { mutableFloatStateOf(0f) }
    var indicatorHeightPx by remember { mutableFloatStateOf(0f) }
    var scrollOffset by rememberSaveable { mutableStateOf(0f) }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val collapseConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (indicatorHeightPx <= 0f) return Offset.Zero

                val current = scrollOffset
                if (available.y < 0f && current < indicatorHeightPx) {
                    val consumed = maxOf(available.y, -(indicatorHeightPx - current))
                    scrollOffset = current - consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (indicatorHeightPx <= 0f) return Offset.Zero

                val current = scrollOffset
                if (available.y > 0f && current > 0f) {
                    val consumed = minOf(available.y, current)
                    scrollOffset = current - consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (indicatorHeightPx <= 0f) return Velocity.Zero

                val current = scrollOffset
                if (current == 0f || current == indicatorHeightPx) return Velocity.Zero

                if (available.y < 0f) {
                    animate(current, indicatorHeightPx) { value, _ ->
                        scrollOffset = value
                    }
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (indicatorHeightPx <= 0f) return Velocity.Zero

                val current = scrollOffset
                if (current <= 0f) return Velocity.Zero

                if (available.y > 0f) {
                    animate(current, 0f) { value, _ ->
                        scrollOffset = value
                    }
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    Scaffold(
        snackbarHost = {
            ErrorSnackbarHost(
                hostState = snackbarHostState,
                rawError = uiState.error?.fullMessage,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = with(LocalDensity.current) { foregroundHeightPx.toDp() })
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                indicatorHeightPx = placeable.height.toFloat()
                                val visibleHeight = (placeable.height - scrollOffset.roundToInt()).coerceAtLeast(0)
                                layout(placeable.width, visibleHeight) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                            .clipToBounds()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(0, -scrollOffset.roundToInt()) }
                        ) {
                            AnimatedContent(
                                targetState = timespanState.selectedTimespan,
                                transitionSpec = {
                                    fun TimespanSelection.weight(): Int = when (this) {
                                        is TimespanSelection.Month -> ym.year * 12 + ym.month
                                        is TimespanSelection.Year -> year * 12 + 6
                                    }
                                    val target = targetState.weight()
                                    val initial = initialState.weight()
                                    if (target > initial) {
                                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                                    } else {
                                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                                    }
                                },
                                label = "widgetAnimation"
                            ) { _ ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragStart = { dragOffset = 0f },
                                                onHorizontalDrag = { change, amount ->
                                                    change.consume()
                                                    dragOffset += amount
                                                },
                                                onDragEnd = {
                                                    val ts = currentTimespanState
                                                    when (val sel = ts.selectedTimespan) {
                                                        is TimespanSelection.Month -> {
                                                            when {
                                                                dragOffset < -swipeThreshold -> {
                                                                    val idx = ts.availableMonths.indexOf(sel.ym)
                                                                    if (idx < ts.availableMonths.size - 1) {
                                                                        onTimespanSelected(TimespanSelection.Month(ts.availableMonths[idx + 1]))
                                                                    }
                                                                }
                                                                dragOffset > swipeThreshold -> {
                                                                    val idx = ts.availableMonths.indexOf(sel.ym)
                                                                    if (idx > 0) {
                                                                        onTimespanSelected(TimespanSelection.Month(ts.availableMonths[idx - 1]))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        is TimespanSelection.Year -> {
                                                            when {
                                                                dragOffset < -swipeThreshold -> {
                                                                    val idx = ts.availableYears.indexOf(sel.year)
                                                                    if (idx < ts.availableYears.size - 1) {
                                                                        onTimespanSelected(TimespanSelection.Year(ts.availableYears[idx + 1]))
                                                                    }
                                                                }
                                                                dragOffset > swipeThreshold -> {
                                                                    val idx = ts.availableYears.indexOf(sel.year)
                                                                    if (idx > 0) {
                                                                        onTimespanSelected(TimespanSelection.Year(ts.availableYears[idx - 1]))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    dragOffset = 0f
                                                }
                                            )
                                        }
                                ) {
                                    CircularIndicator(
                                        currency = selectedCurrency,
                                        transactions = transactionListState.transactions,
                                        selectedTimespan = timespanState.selectedTimespan,
                                        onCategoryClick = onCategoryClick,
                                        selectedCategoryId = selectedCategoryId,
                                        isUncategorizedSelected = isUncategorizedSelected,
                                    )
                                }
                            }
                        }
                    }
                }

                LazyTransactionList(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    isRefreshing = filteredTransactionListState.isRefreshing,
                    transactions = filteredTransactionListState.transactions,
                    navigateToDetails = navigateToDetails,
                    onRefresh = onRefresh,
                    onDeleteTransaction = onDeleteTransaction,
                    listState = listState,
                    collapseConnection = collapseConnection
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .onGloballyPositioned { foregroundHeightPx = it.size.height.toFloat() }
            ) {
                TopContent()
                TimespanBar(
                    selectedTimespan = timespanState.selectedTimespan,
                    availableMonths = timespanState.availableMonths,
                    availableYears = timespanState.availableYears,
                    isYearView = timespanState.isYearView,
                    onTimespanSelected = onTimespanSelected,
                    isLoadingMore = transactionListState.isLoadingMore,
                    onToggleView = onToggleView,
                    onLoadMore = onLoadMore
                )
                LoadingProgressIndicator(
                    isLoading = filteredTransactionListState.isLoading || transactionListState.isLoadingMore
                )
            }
        }
    }
}


@Composable
private fun SwipableTransactionRow(
    transaction: Transaction,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onClick: () -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }

    val buttonWidth = 85.dp
    val density = LocalDensity.current
    val buttonWidthPx = with(density) { buttonWidth.toPx() }
    val isDeleteTransaction = remember { mutableStateOf(false) }

    if (isDeleteTransaction.value) {
        TransactionDeleteDialog(
            transactionId = transaction.id,
            onDismiss = isDeleteTransaction,
            onTransactionDelete = onDeleteTransaction
        )
    }
    LaunchedEffect(isOpen) {
        if (!isOpen) {
            offsetX = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        IconButton(
            onClick = { isDeleteTransaction.value = true },
            modifier = Modifier.align(Alignment.CenterStart)
                .background(Color.Red)
                .width(85.dp)
                .height(32.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Main Transaction Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount)
                                .coerceIn(0f, buttonWidthPx)
                        },
                        onDragEnd = {
                            offsetX = when {
                                offsetX > buttonWidthPx / 2 -> {
                                    onOpen()
                                    buttonWidthPx
                                }
                                else -> {
                                    onClose()
                                    0f
                                }
                            }
                        }
                    )
                }
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = transaction.remittanceInformationUnstructured.replace("\\s+".toRegex(), " "),
                        color = MaterialTheme.colorScheme.primary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BankLogo(
                        logoUrl = transaction.bankLogoUrl,
                        bankName = transaction.bankName,
                        size = 22
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tag = transaction.expenseTag
                    Icon(
                        painter = painterResource(
                            if (tag != null) Res.drawable.ic_tag_filled else Res.drawable.ic_tag
                        ),
                        contentDescription = tag?.name,
                        tint = tag?.color ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${transaction.amount} ${currencyDisplayForCode(transaction.currency)}",
                        color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TopContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TimespanBar(
    selectedTimespan: TimespanSelection,
    availableMonths: List<YearMonth>,
    availableYears: List<Int>,
    isYearView: Boolean,
    isLoadingMore: Boolean,
    onTimespanSelected: (TimespanSelection) -> Unit,
    onToggleView: () -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    var overscrollOffset by remember { mutableFloatStateOf(0f) }
    val overscrollThreshold = 100f

    val isAtEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisible != null && lastVisible.index == layoutInfo.totalItemsCount - 1
        }
    }

    val nestedScrollConnection = remember(isAtEnd, isLoadingMore, overscrollThreshold) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (isAtEnd && !isLoadingMore && source == NestedScrollSource.UserInput) {
                    val overscroll = -available.x
                    if (overscroll > 0) {
                        overscrollOffset = (overscrollOffset + overscroll).coerceAtMost(overscrollThreshold)
                        return Offset(available.x, 0f)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (isAtEnd && overscrollOffset >= overscrollThreshold && !isLoadingMore) {
                    onLoadMore()
                }
                overscrollOffset = 0f
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(selectedTimespan, isYearView) {
        overscrollOffset = 0f
        val index = when (val sel = selectedTimespan) {
            is TimespanSelection.Month -> availableMonths.indexOf(sel.ym)
            is TimespanSelection.Year -> availableYears.indexOf(sel.year)
        }
        if (index != -1) {
            listState.animateScrollToItem(index)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .nestedScroll(nestedScrollConnection),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isYearView) {
                items(availableYears, key = { it }) { year ->
                    MonthChip(
                        label = year.toString(),
                        selected = (selectedTimespan as? TimespanSelection.Year)?.year == year,
                        onClick = { onTimespanSelected(TimespanSelection.Year(year)) }
                    )
                }
            } else {
                items(availableMonths) { ym ->
                    val monthName = Month(ym.month).name.take(3)
                    MonthChip(
                        label = "$monthName ${ym.year}",
                        selected = (selectedTimespan as? TimespanSelection.Month)?.ym == ym,
                        onClick = { onTimespanSelected(TimespanSelection.Month(ym)) }
                    )
                }
            }
        }

        val pullProgress = (overscrollOffset / overscrollThreshold).coerceIn(0f, 1f)
        if (pullProgress > 0f && !isLoadingMore) {
            val indicatorAlpha by animateFloatAsState(
                targetValue = pullProgress.coerceIn(0.3f, 1f),
                animationSpec = tween(durationMillis = 300)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
                    .alpha(indicatorAlpha)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    progress = pullProgress
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 0.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = if (isYearView) "Year" else "Month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Switch(
                checked = isYearView,
                onCheckedChange = { onToggleView() },
                modifier = Modifier.scale(0.7f)
            )
        }
    }
}

@Composable
private fun MonthChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    }
}

private val NoOpNestedScrollConnection = object : NestedScrollConnection {}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private fun LazyTransactionList(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    transactions: List<ModelTransaction>,
    navigateToDetails: (ModelTransaction) -> Unit,
    onRefresh: () -> Unit,
    onDeleteTransaction: (String) -> Unit,
    listState: LazyListState,
    collapseConnection: NestedScrollConnection = NoOpNestedScrollConnection
) {
    var openedRowId by remember { mutableStateOf<String?>(null) }
    val groupedTransactions = remember(transactions) { transactions.groupBy { it.bookingDate.date } }

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(collapseConnection)
                .pointerInput(openedRowId) {
                    if (openedRowId != null) {
                        detectTapGestures {
                            openedRowId = null
                        }
                    }
                }
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
                        key = { it.id }
                    ) { transaction ->
                        SwipableTransactionRow(
                            transaction = transaction,
                            isOpen = openedRowId == transaction.id,
                            onOpen = { openedRowId = transaction.id },
                            onClose = { openedRowId = null },
                            onClick = { navigateToDetails(transaction) },
                            onDeleteTransaction = onDeleteTransaction
                        )
                    }
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
