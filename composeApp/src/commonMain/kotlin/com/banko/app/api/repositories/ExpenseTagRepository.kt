package com.banko.app.api.repositories

import com.banko.app.api.dto.bankoApi.ExpenseTag
import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExpenseTagRepository(
    private val apiService: BankoApiService = BankoApiService()
) {
    suspend fun getExpenseTags(): List<ExpenseTag> {
        val result = apiService.getExpenseTags()
        if (result is Result.Error) {
            println("Error: ${result.error}")
            return emptyList()
        }

        return (result as Result.Success).data.expenseTags
    }

    suspend fun updateExpenseTag(expenseTag: ExpenseTag): ExpenseTag? {
        val result = apiService.updateExpenseTag(expenseTag)
        if (result is Result.Error) {
            println("Error: ${result.error}")
            return null
        }

        return (result as Result.Success).data.expenseTag
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createExpenseTag(name: String, color: Long): ExpenseTag? {
        val tag = ExpenseTag(
            id = Uuid.random().toString(),
            name = name,
            color = color,
            aka = null
        )
        val result = apiService.createExpenseTag(tag)
        if (result is Result.Error) {
            println("Error: ${result.error}")
            return null
        }

        return (result as Result.Success).data.expenseTag
    }

    suspend fun deleteExpenseTag(expenseTagId: String) {
        val result = apiService.deleteExpenseTag(expenseTagId)
        if (result is Result.Error) {
            println("Error: ${result.error}")
            return
        }
    }
}