package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.banko.app.ApiTransactionRepository
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.ModelTransaction
import com.banko.app.api.dto.bankoApi.Transactions
import com.banko.app.api.dto.bankoApi.toModelItem
import com.banko.app.ui.models.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private const val pageSize = 30

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    private val dbRepository: DatabaseTransactionRepository,
) : ViewModel() {
    private val _screenState = MutableStateFlow(HomeScreenState())
    val screenState: StateFlow<HomeScreenState> = _screenState
    private var pageNumber: Int? = null

    val pagingDataFlow: Flow<PagingData<TransactionPagingData>> =
        dbRepository.getTransactionsPagingSource(pageSize = pageSize, pageNumber = pageNumber) {
            pageNumber = it
        }.map { pagingData ->
            var counter = 0
            pagingData.insertSeparators(TerminalSeparatorType.SOURCE_COMPLETE) { before, after ->
                val dateBefore = before?.bookingDate?.date
                val dateAfter = after?.bookingDate?.date

                if (dateAfter != null && dateBefore != dateAfter) dateAfter else null
            }.map { item ->
                when (item) {
                    is ModelTransaction -> TransactionPagingData.Item(item)
                    is LocalDate -> {
                        TransactionPagingData.Separator(item)
                    }
                    else -> error("Unknown item type: $item")
                }
            }
        }.cachedIn(viewModelScope)
}

sealed interface TransactionPagingData {
    data class Item(
        val modelTransaction: ModelTransaction
    ) : TransactionPagingData

    data class Separator(
        val date: LocalDate
    ) : TransactionPagingData
}