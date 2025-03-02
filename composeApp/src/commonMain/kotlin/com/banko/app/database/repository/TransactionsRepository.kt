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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

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

    fun getAllTransactions(limit: Int): Flow<List<FullTransaction>> = flow {
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
                    FullTransaction(
                        transaction = transaction.transaction,
                        creditorAccount = creditor,
                        debtorAccount = debtor,
                        expenseTag = expenseTag
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

    suspend fun getTransactionCount(): Long = dao.getTransactionCount()
}
