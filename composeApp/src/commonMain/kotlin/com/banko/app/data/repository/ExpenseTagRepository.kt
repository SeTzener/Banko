package com.banko.app.data.repository

import com.banko.app.data.local.ExpenseTagLocalDataSource
import com.banko.app.data.remote.ExpenseTagRemoteDataSource
import com.banko.app.domain.model.ExpenseTag
import kotlinx.coroutines.flow.Flow

class ExpenseTagRepository(
    private val local: ExpenseTagLocalDataSource,
    private val remote: ExpenseTagRemoteDataSource
) {
    fun getAllExpenseTags(): Flow<List<ExpenseTag>> =
        local.getAllExpenseTags()

    suspend fun getExpenseTagById(expenseTagId: String): ExpenseTag? =
        local.getExpenseTagById(expenseTagId)

    suspend fun refreshExpenseTags() {
        remote.fetchExpenseTags().forEach { local.upsertExpenseTag(it) }
    }

    suspend fun updateExpenseTag(expenseTag: ExpenseTag) {
        local.upsertExpenseTag(remote.updateExpenseTag(expenseTag))
    }

    suspend fun createExpenseTag(name: String, color: Long, isEarning: Boolean) {
        local.upsertExpenseTag(remote.createExpenseTag(name, color, isEarning))
    }

    suspend fun deleteExpenseTag(expenseTagId: String) {
        remote.deleteExpenseTag(expenseTagId)
        local.deleteExpenseTag(expenseTagId)
    }

    suspend fun assignExpenseTag(transactionId: String, expenseTagId: String?) {
        remote.assignExpenseTag(transactionId, expenseTagId)
    }
}
