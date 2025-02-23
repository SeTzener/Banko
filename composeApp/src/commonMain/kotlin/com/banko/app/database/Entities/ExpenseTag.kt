package com.banko.app.database.Entities

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

typealias ModelExpenseTag = com.banko.app.ui.models.ExpenseTag

@Entity(tableName = "expense_tag")
data class ExpenseTag (
    @PrimaryKey
    val id: String,
    val name: String,
    val color: Long,
    val aka: List<String>?
)

fun ExpenseTag.toModel() = ModelExpenseTag (
    id = id,
    name = name,
    color = Color(color),
    aka = aka ?: emptyList()
)