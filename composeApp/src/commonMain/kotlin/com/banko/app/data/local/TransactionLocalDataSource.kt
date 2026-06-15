package com.banko.app.data.local

import com.banko.app.database.BankoDatabase
import com.banko.app.data.mapper.toDao
import com.banko.app.data.mapper.toDomain
import com.banko.app.domain.model.Transaction
import com.banko.app.utils.getLastDayOfMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.banko.app.utils.now
import kotlinx.datetime.LocalDateTime

class TransactionLocalDataSource(
    private val database: BankoDatabase
) {
    private val dao = database.bankoDao()

    fun getTransactions(limit: Int): Flow<List<Transaction>> =
        dao.getTransactionsPagingSource(limit)
            .map { it.toDomain() }

    fun getTransactionsForMonth(date: LocalDateTime): Flow<List<Transaction>> {
        val monthStart = LocalDateTime(date.year, date.monthNumber, 1, 0, 0).toString()
        val monthEnd = LocalDateTime(
            date.year,
            date.monthNumber,
            getLastDayOfMonth(date.year, date.monthNumber),
            23, 59
        ).toString()
        return dao.getTransactionsForMonth(monthStart, monthEnd)
            .map { it.toDomain() }
    }

    suspend fun getStoredTransactionCount(): Long = dao.getTransactionCount()

    suspend fun getOldestTransactions(): LocalDateTime {
        val result = dao.getOldestTransactions() ?: return LocalDateTime.now
        return LocalDateTime.parse(result)
    }

    suspend fun upsertTransaction(transaction: Transaction) {
        transaction.creditorAccount?.let { dao.upsertCreditorAccount(it.toDao()) }
        transaction.debtorAccount?.let { dao.upsertDebtorAccount(it.toDao()) }
        transaction.expenseTag?.let { dao.upsertExpenseTag(it.toDao()) }
        dao.upsertTransaction(transaction.toDao())
    }

    suspend fun deleteTransaction(transactionId: String) {
        dao.deleteTransaction(transactionId)
    }
}
