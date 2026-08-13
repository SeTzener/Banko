package com.banko.app.data.local

import com.banko.app.data.mapper.toDao
import com.banko.app.data.mapper.toDomain
import com.banko.app.database.BankoDatabase
import com.banko.app.domain.model.ExpenseTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ExpenseTagLocalDataSource(
    private val database: BankoDatabase
) {
    private val dao = database.bankoDao()

    fun getAllExpenseTags(): Flow<List<ExpenseTag>> =
        dao.getAllExpenseTags()
            .map { tags -> tags.map { it.toDomain() } }

    suspend fun getExpenseTagById(expenseTagId: String): ExpenseTag? =
        dao.getExpenseTagById(expenseTagId).first()?.toDomain()

    suspend fun upsertExpenseTag(expenseTag: ExpenseTag) {
        dao.upsertExpenseTag(expenseTag.toDao())
    }

    suspend fun deleteExpenseTag(expenseTagId: String) {
        val expenseTag = dao.getExpenseTagById(expenseTagId).first() ?: return
        dao.deleteExpenseTag(expenseTag)
    }
}
