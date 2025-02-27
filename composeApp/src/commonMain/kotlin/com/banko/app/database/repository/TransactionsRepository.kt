package com.banko.app.database.repository

import com.banko.app.DaoCreditorAccount
import com.banko.app.DaoDebtorAccount
import com.banko.app.DaoExpenseTag
import com.banko.app.DaoTransaction
import com.banko.app.ModelTransaction
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.FullTransaction
import com.banko.app.ui.models.toDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransactionsRepository(
    private val bankoDatabase: BankoDatabase,

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

    suspend fun getAllTransactions(pageNumber: Int, pageSize: Int): Flow<List<FullTransaction>> = flow {
        val offset = (pageNumber - 1) * pageSize
        val limit = pageSize

        dao.getAllTransactions(offset = offset, limit = limit).collect { transactions ->
            val fullTransactions = transactions.map { transaction ->
                FullTransaction(
                    transaction = transaction.transaction,
                    creditorAccount = transaction.transaction.creditorAccountId?.let {
                        dao.getCreditorAccountById(it)
                    },
                    debtorAccount = transaction.transaction.debtorAccountId?.let {
                        dao.getDebtorAccountById(it)
                    },
                    expenseTag = transaction.transaction.expenseTagId?.let {
                        dao.getExpenseTagById(it)
                    }
                )
            }
            emit(fullTransactions)
        }
    }

    suspend fun findRawTransactionById(transactionId: String): DaoTransaction? {
        return dao.getRawTransactionById(transactionId)
    }

    suspend fun getTransactionCount(): Long = dao.getTransactionCount()
}
