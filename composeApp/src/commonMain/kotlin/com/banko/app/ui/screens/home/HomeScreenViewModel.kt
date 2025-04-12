package com.banko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.banko.app.DatabaseTransactionRepository
import com.banko.app.ModelTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

private const val pageSize = 30

class HomeScreenViewModel(
    private val dbRepository: DatabaseTransactionRepository,
) : ViewModel() {
    private val _screenState = MutableStateFlow(HomeScreenState())
    val screenState: StateFlow<HomeScreenState> = _screenState

    val pagingDataFlow: Flow<PagingData<TransactionPagingData>> =
        dbRepository.getTransactionsPagingSource(
            pageSize = pageSize,
        )
            .map { pagingData ->
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