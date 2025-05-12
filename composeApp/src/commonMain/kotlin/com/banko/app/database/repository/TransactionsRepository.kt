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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        return LocalDateTime.parse(dao.getOldestTransactions())
    }

    suspend fun fetchAndStoreTransactions(
        pageNumber: Int,
        pageSize: Int
    ): Result<Long, NetworkError> {
        val result = apiService.getTransactions(pageNumber = pageNumber, pageSize = pageSize)
        when (result) {
            is Result.Error -> {
                println("Error: ${result.error}")
                return result
            }

            is Result.Success -> {
                val transactions = result.data
                if (transactions.totalCount == 0L) return Result.Error(NetworkError.NO_NEW_TRANSACTIONS)

                result.data.transactions.forEach { transaction ->
                    upsertTransaction(transaction.toModelItem())
                }
                Result.Success(result.data.totalCount <= (pageNumber * pageSize))

                return Result.Success(transactions.totalCount)
            }
        }
    }

    suspend fun getStoredTransactionCount(): Long {
        return dao.getTransactionCount()
    }
}
