package com.banko.app.api.dto.bankoApi

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseTags (
    val expenseTags: List<ExpenseTag>
)

@Serializable
data class ExpenseTag (
    val id: String,
    val name: String,
    val color: Long,
    val aka: List<String>?
)

@Serializable
data class UpsertExpenseTag (
    val expenseTag: ExpenseTag
)