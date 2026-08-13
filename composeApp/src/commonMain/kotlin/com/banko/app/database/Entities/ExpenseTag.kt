package com.banko.app.database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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