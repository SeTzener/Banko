package com.banko.app.data.remote

import com.banko.app.api.services.BankoApiService
import com.banko.app.api.utils.Result
import com.banko.app.data.mapper.toDomain
import com.banko.app.data.mapper.toDto
import com.banko.app.domain.model.ExpenseTag

class ExpenseTagRemoteDataSource(
    private val apiService: BankoApiService
) {
    suspend fun fetchExpenseTags(): List<ExpenseTag> =
        when (val result = apiService.getExpenseTags()) {
            is Result.Error -> throw RuntimeException("Failed to get expense tags: $result")
            is Result.Success -> result.value.expenseTags.map { it.toDomain() }
        }

    suspend fun updateExpenseTag(expenseTag: ExpenseTag): ExpenseTag =
        when (val result = apiService.updateExpenseTag(expenseTag.toDto())) {
            is Result.Error -> throw RuntimeException("Failed to update expense tag: $result")
            is Result.Success -> result.value.expenseTag.toDomain()
        }

    suspend fun createExpenseTag(name: String, color: Long, isEarning: Boolean): ExpenseTag =
        when (val result = apiService.createExpenseTag(name, color, isEarning)) {
            is Result.Error -> throw RuntimeException("Failed to create expense tag: $result")
            is Result.Success -> result.value.expenseTag.toDomain()
        }

    suspend fun deleteExpenseTag(expenseTagId: String) {
        when (val result = apiService.deleteExpenseTag(expenseTagId)) {
            is Result.Error -> throw RuntimeException("Failed to delete expense tag: $result")
            is Result.Success -> Unit
        }
    }
}
