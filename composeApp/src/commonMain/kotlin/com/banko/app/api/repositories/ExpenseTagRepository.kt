package com.banko.app.api.repositories

import com.banko.app.api.dto.bankoApi.toModelItem
import com.banko.app.api.services.BankoApiService
import com.banko.app.ui.models.ExpenseTag
import com.banko.app.ui.models.toDto
import kotlin.uuid.ExperimentalUuidApi
import com.banko.app.api.utils.Result

class ExpenseTagRepository(
    private val apiService: BankoApiService
) {
    suspend fun getExpenseTags(): List<ExpenseTag> {
        val result = apiService.getExpenseTags()
        if (result is Result.Error) {
            throw RuntimeException("Failed to get expense tags: $result")
        }

        return (result as Result.Success).value.expenseTags.map { it.toModelItem() }
    }

    suspend fun updateExpenseTag(expenseTag: ExpenseTag): ExpenseTag {
        val result = apiService.updateExpenseTag(expenseTag.toDto())
        if (result is Result.Error) {
            throw RuntimeException("Failed to update expense tag: $result")
        }

        return (result as Result.Success).value.expenseTag.toModelItem()
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createExpenseTag(name: String, color: Long, isEarning: Boolean): ExpenseTag {
        val result = apiService.createExpenseTag(name, color, isEarning)
        if (result is Result.Error) {
            throw RuntimeException("Failed to create expense tag: $result")
        }

        return (result as Result.Success).value.expenseTag.toModelItem()
    }

    suspend fun deleteExpenseTag(expenseTagId: String) {
        val result = apiService.deleteExpenseTag(expenseTagId)
        if (result is Result.Error) {
            throw RuntimeException("Failed to delete expense tag: $result")
        }
    }

    suspend fun assignExpenseTag(id: String, expenseTagId: String?) {
        val result = apiService.assignExpenseTag(id, expenseTagId)
        if (result is Result.Error) {
            throw RuntimeException("Failed to assign expense tag: $result")
        }
    }
}