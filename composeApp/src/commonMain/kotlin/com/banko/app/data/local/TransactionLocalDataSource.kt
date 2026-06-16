package com.banko.app.data.local

import com.banko.app.database.BankoDatabase
import com.banko.app.data.mapper.toDao
import com.banko.app.data.mapper.toDomain
import com.banko.app.domain.model.Transaction
import com.banko.app.utils.now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus

class TransactionLocalDataSource(
    private val database: BankoDatabase
) {
    private val dao = database.bankoDao()

    fun getTransactions(limit: Int): Flow<List<Transaction>> =
        dao.getTransactionsPagingSource(limit)
            .map { it.toDomain() }

    suspend fun getStoredTransactionCount(): Long = dao.getTransactionCount()

    suspend fun getOldestTransactions(): LocalDateTime {
        val result = dao.getOldestTransactions() ?: return LocalDateTime.now
        return LocalDateTime.parse(result)
    }

    fun getTransactionsForDateRange(fromDate: LocalDate, toDate: LocalDate): Flow<List<Transaction>> {
        val endExclusive = toDate.plus(DatePeriod(days = 1))
        return dao.getTransactionsForMonth(fromDate.toString(), endExclusive.toString())
            .map { it.toDomain() }
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
