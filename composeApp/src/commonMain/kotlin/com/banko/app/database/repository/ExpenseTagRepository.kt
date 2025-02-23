package com.banko.app.database.repository

import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExpenseTag
import kotlinx.coroutines.flow.Flow

class ExpenseTagRepository(
    private val bankoDatabase: BankoDatabase,
) {
    fun getAllExpenseTags(): Flow<List<ExpenseTag?>> =
        bankoDatabase.bankoDao().getAllExpenseTags()

    suspend fun upsertExpenseTag(expenseTag: ExpenseTag) {
        bankoDatabase.bankoDao().upsertExpenseTag(expenseTag)
    }
}