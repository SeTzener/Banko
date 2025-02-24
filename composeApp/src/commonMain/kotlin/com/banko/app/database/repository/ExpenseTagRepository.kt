package com.banko.app.database.repository

import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExpenseTag
import kotlinx.coroutines.flow.Flow

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
        val tag = dao.getExpenseTagById(expenseTagId) ?: return
        dao.deleteExpenseTag(expenseTag = tag)
    }

    suspend fun findExpenseTagById(expenseTagId: String?): ExpenseTag? {
        expenseTagId ?: return null
        return dao.getExpenseTagById(expenseTagId)
    }
}