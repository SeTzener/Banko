package com.banko.app.database.repository

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
import com.banko.app.database.Entities.toModelItem
import com.banko.app.ui.models.toDao
import com.banko.app.utils.getLastDayOfMonth
import com.banko.app.utils.now
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class TransactionsRepository(
    private val bankoDatabase: BankoDatabase,
    private val apiService: BankoApiService
) {
    private val dispatchers = Dispatchers.IO
    private val dao = bankoDatabase.bankoDao()

    private suspend fun upsertTransaction(transaction: ModelTransaction) {
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

    fun getLocalTransactions(limit: Int): Flow<List<ModelTransaction>> {
        return dao.getTransactionsPagingSource(limit)
            .map { list -> list.map { it.toModelItem() } }
    }

    suspend fun getOldestTransactions(): LocalDateTime {
        val result = dao.getOldestTransactions() ?: return LocalDateTime.now
        return LocalDateTime.parse(result)
    }

    suspend fun fetchAndStoreTransactions(
        pageNumber: Int,
        pageSize: Int
    ): Result<Long> {
        val result = apiService.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
        when (result) {
            is Result.Error -> {
                println("Error: $result")
                return result
            }

            is Result.Success -> {
                val transactions = result.value
                if (transactions.totalCount == 0L) return Result.Error.NetworkError(Exception(NetworkError.NO_NEW_TRANSACTIONS.toString()))

                result.value.transactions.forEach { transaction ->
                    upsertTransaction(transaction.toModelItem())
                }
                Result.Success(result.value.totalCount <= (pageNumber * pageSize))

                return Result.Success(transactions.totalCount)
            }
        }
    }

    suspend fun getStoredTransactionCount(): Long {
        return dao.getTransactionCount()
    }

    fun getTransactionsForMonth(month: LocalDateTime, year: Int): Flow<List<ModelTransaction>> {
        val monthStart = LocalDateTime(year, month.monthNumber, 1, 0, 0).toString()
        val monthEnd = LocalDateTime(year, month.monthNumber, getLastDayOfMonth(year, month.monthNumber), 23, 59).toString()
        return dao.getTransactionsForMonth(monthStart, monthEnd).map { list -> list.map { it.toModelItem() } ?: emptyList() }
    }
}
