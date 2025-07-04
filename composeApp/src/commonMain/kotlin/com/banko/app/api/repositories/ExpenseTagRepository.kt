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
            println("Error: $result")
            return emptyList()
        }

        return (result as Result.Success).value.expenseTags.map { it.toModelItem() }
    }

    suspend fun updateExpenseTag(expenseTag: ExpenseTag): ExpenseTag? {
        val result = apiService.updateExpenseTag(expenseTag.toDto())
        if (result is Result.Error) {
            println("Error: $result")
            return null
        }

        return (result as Result.Success).value.expenseTag.toModelItem()
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createExpenseTag(name: String, color: Long): ExpenseTag? {
        val result = apiService.createExpenseTag(name, color)
        if (result is Result.Error) {
            println("Error: $result")
            return null
        }

        return (result as Result.Success).value.expenseTag.toModelItem()
    }

    suspend fun deleteExpenseTag(expenseTagId: String) {
        val result = apiService.deleteExpenseTag(expenseTagId)
        if (result is Result.Error) {
            println("Error: $result")
            return
        }
    }

    suspend fun assignExpenseTag(id: String, expenseTagId: String?) {
        val result = apiService.assignExpenseTag(id, expenseTagId)
        if (result is Result.Error) {
            println("Error: $result")
            return
        }
    }
}