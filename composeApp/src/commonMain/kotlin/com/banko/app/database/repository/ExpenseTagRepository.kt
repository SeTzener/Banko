package com.banko.app.database.repository

import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExpenseTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ExpenseTagRepository(
    private val bankoDatabase: BankoDatabase,
) {
    private val dao = bankoDatabase.bankoDao()
    fun getAllExpenseTags(): Flow<List<ExpenseTag?>> =
        dao.getAllExpenseTags()

    suspend fun upsertExpenseTag(expenseTag: ExpenseTag) {
        dao.upsertExpenseTag(expenseTag)
    }

    suspend fun deleteExpenseTag(expenseTagId: String) {
        dao.getExpenseTagById(expenseTagId).collect { expenseTag ->
            expenseTag ?: return@collect
            dao.deleteExpenseTag(expenseTag = expenseTag)
        }
    }

    suspend fun findExpenseTagById(expenseTagId: String?): Flow<ExpenseTag?> {
        expenseTagId ?: return flowOf(null)
        return dao.getExpenseTagById(expenseTagId)
    }
}