package com.banko.app.database.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.TerminalSeparatorType
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime

class TransactionsRepository(
    private val bankoDatabase: BankoDatabase,
    private val apiService: BankoApiService

) {
    private val dispatchers = Dispatchers.IO
    private val dao = bankoDatabase.bankoDao()

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

    fun getAllTransactions(limit: Int): Flow<List<ModelTransaction>> = flow {
        // Emit the transactions from the DAO
        val transactions = dao.getAllTransactions(limit = limit)

        // Process the transactions and fetch related data for each one
        transactions.collect { transactionList ->
            val fullTransactionFlows = transactionList.map { transaction ->
                // Create flows for each related entity (creditor, debtor, expense tag)
                val creditorFlow = transaction.transaction.creditorAccountId?.let {
                    dao.getCreditorAccountById(it)
                } ?: flowOf(null)

                val debtorFlow = transaction.transaction.debtorAccountId?.let {
                    dao.getDebtorAccountById(it)
                } ?: flowOf(null)

                val expenseTagFlow = transaction.transaction.expenseTagId?.let {
                    dao.getExpenseTagById(it)
                } ?: flowOf(null)

                // Combine them to create FullTransaction
                combine(creditorFlow, debtorFlow, expenseTagFlow) { creditor, debtor, expenseTag ->
                    ModelTransaction(
                        id = transaction.transaction.id,
                        bookingDate = LocalDateTime.parse(transaction.transaction.bookingDate),
                        valueDate = LocalDateTime.parse(transaction.transaction.valueDate),
                        amount = transaction.transaction.amount.toDouble(),
                        currency = transaction.transaction.currency,
                        creditorAccount = creditor?.toModelItem(),
                        debtorAccount = debtor?.toModelItem(),
                        expenseTag = expenseTag?.toModelItem(),
                        remittanceInformationUnstructured = transaction.transaction.remittanceInformationUnstructured,
                        bankTransactionCode = transaction.transaction.bankTransactionCode,
                        internalTransactionId = transaction.transaction.internalTransactionId,
                        creditorName = transaction.transaction.creditorName,
                        debtorName = transaction.transaction.debtorName,
                        remittanceInformationUnstructuredArray = transaction.transaction.remittanceInformationUnstructuredArray,
                        remittanceInformationStructuredArray = transaction.transaction.remittanceInformationStructuredArray
                    )
                }
            }

            // Combine all the flows into a list and emit it
            combine(fullTransactionFlows) { it.toList() }.collect { fullTransactions ->
                emit(fullTransactions)
            }
        }
    }


    suspend fun findRawTransactionById(transactionId: String): DaoTransaction? {
        return dao.getRawTransactionById(transactionId)
    }

    private suspend fun getTransactionCount(): Long = dao.getTransactionCount()

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
            // There's also the possibility to change this to
            // transactions.totalCount > (pageNumber * pageSize)
            transactions.totalCount > getTransactionCount()
        )
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getTransactionsPagingSource(
        pageSize: Int,
        pageNumber: Int?,
        updatePageNumber: (Int) -> Unit
    ): Flow<PagingData<ModelTransaction>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize),
            pagingSourceFactory = { dao.getTransactionsPagingSource() },
            remoteMediator = object : RemoteMediator<Int, FullTransaction>() {
                override suspend fun load(
                    loadType: LoadType,
                    state: PagingState<Int, FullTransaction>
                ): MediatorResult {
                    return when (loadType) {
                        LoadType.PREPEND -> {
                            MediatorResult.Success(true)
                        }

                        LoadType.REFRESH,
                        LoadType.APPEND -> {
                            val localPageNumber =
                                if (pageNumber == null || loadType == LoadType.REFRESH) 1 else pageNumber

                            val result =
                                fetchAndStoreTransactions(
                                    pageNumber = localPageNumber,
                                    pageSize = pageSize
                                )
                            if (result is Result.Success) {
                                updatePageNumber(localPageNumber)
                                return MediatorResult.Success(result.data)
                            } else {
                                return MediatorResult.Error(Exception("Failed to fetch transactions"))
                            }
                        }
                    }
                }
            }
        ).flow.map { pagingData -> pagingData.map { it.toModelItem() } }
    }
}
