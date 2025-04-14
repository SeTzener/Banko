package com.banko.app.database.repository

import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.map
import app.cash.paging.MAX_SIZE_UNBOUNDED
import com.banko.app.DaoCreditorAccount
import com.banko.app.DaoDebtorAccount
import com.banko.app.DaoExpenseTag
import com.banko.app.DaoTransaction
import com.banko.app.ModelTransaction
import com.banko.app.api.dto.bankoApi.toModelItem
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.NetworkError
import com.banko.app.api.utils.Result
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.FullTransaction
import com.banko.app.database.Entities.toModelItem
import com.banko.app.ui.models.toDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

class TransactionsRepository(
    private val bankoDatabase: BankoDatabase,
    private val apiService: BankoApiService
) {
    private val dispatchers = Dispatchers.IO
    private val dao = bankoDatabase.bankoDao()
    var pageNumber: Int? = null

    suspend fun upsertTransaction(transaction: ModelTransaction) {
        transaction.creditorAccount?.let { dao.upsertCreditorAccount(it.toDao()) }
        transaction.debtorAccount?.let { dao.upsertDebtorAccount(it.toDao()) }
        transaction.expenseTag?.let { dao.upsertExpenseTag(it.toDao()) }

        dao.upsertTransaction(transaction.toDao())
    }

    suspend fun upsertTransaction(
        transaction: DaoTransaction,
        creditorAccount: DaoCreditorAccount? = null,
        debtorAccount: DaoDebtorAccount? = null,
        expenseTag: DaoExpenseTag? = null
    ) {
        if (creditorAccount != null) {
            dao.upsertCreditorAccount(creditorAccount)
        }
        if (debtorAccount != null) {
            dao.upsertDebtorAccount(debtorAccount)
        }
        if (expenseTag != null) {
            dao.upsertExpenseTag(expenseTag)
        }

        dao.upsertTransaction(transaction)
    }

    suspend fun findRawTransactionById(transactionId: String): DaoTransaction? {
        return dao.getRawTransactionById(transactionId)
    }

    suspend fun fetchAndStoreTransactions(
        pageNumber: Int,
        pageSize: Int
    ): Result<Boolean, NetworkError> {
        val result = apiService.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
        if (result is Result.Error) {
            println("Error: ${result.error}")
            return Result.Error(result.error)
        }
        val transactions = (result as Result.Success).data
        if (transactions.totalCount == 0L) return Result.Error(NetworkError.NO_NEW_TRANSACTIONS)

        transactions.let {
            it.transactions.forEach { transaction ->
                upsertTransaction(transaction.toModelItem())
            }
        }
        return Result.Success(
            transactions.totalCount <= (pageNumber * pageSize)
        )
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getTransactionsPagingSource(
        pageSize: Int,
        coroutineScope: CoroutineScope,
    ): Flow<PagingData<ModelTransaction>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize),
            pagingSourceFactory = {
                coroutineScope.launch {
                    val count = dao.transactionsCount()
                    println("count $count")
                }
                dao.getTransactionsPagingSource()
            },
            remoteMediator = object : RemoteMediator<Int, FullTransaction>() {
                override suspend fun load(
                    loadType: LoadType,
                    state: PagingState<Int, FullTransaction>
                ): MediatorResult {
                    return when (loadType) {
                        LoadType.PREPEND -> {
                            println("PREPEND")
                            MediatorResult.Success(true)
                        }

                        LoadType.REFRESH -> {
                            println("REFRESH")
                            val result = fetchAndStoreTransactions(
                                pageNumber = 1,
                                pageSize = pageSize
                            )
                            if (result is Result.Success) {
                                return MediatorResult.Success(result.data)
                            } else {
                                return MediatorResult.Error(Exception("Failed to fetch transactions"))
                            }
                        }

                        LoadType.APPEND -> {
                            println("APPEND")
                            val localPageNumber =
                                if (pageNumber == null) 1 else pageNumber

                            val result =
                                localPageNumber?.let {
                                    fetchAndStoreTransactions(
                                        pageNumber = it,
                                        pageSize = pageSize
                                    )
                                }
                            if (result is Result.Success) {
                                pageNumber = localPageNumber + 1
                                return MediatorResult.Success(result.data)
                            } else {
                                println("error")
                                return MediatorResult.Error(Exception("Failed to fetch transactions"))
                            }
                        }
                    }
                }
            }
        ).flow.map { pagingData ->
            pagingData.map {
                it.toModelItem()
            }
        }
    }
}
