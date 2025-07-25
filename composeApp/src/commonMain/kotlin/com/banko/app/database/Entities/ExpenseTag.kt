package com.banko.app.database.Entities

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banko.app.ModelExpenseTag

@Entity(tableName = "expense_tag")
data class ExpenseTag (
    @PrimaryKey
    val id: String,
    val name: String,
    val color: Long,
    @ColumnInfo(defaultValue = "0")
    val isEarning: Boolean?,
    val aka: List<String>?
)

fun ExpenseTag.toModelItem() = ModelExpenseTag (
    id = id,
    name = name,
    color = Color(color),
    isEarning = isEarning,
    aka = aka ?: emptyList()
)