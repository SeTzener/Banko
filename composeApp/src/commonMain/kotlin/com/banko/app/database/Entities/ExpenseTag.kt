package com.banko.app.database.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_tag")
data class ExpenseTag (
    @PrimaryKey
    val id: String,
    val name: String,
    val color: Long,
    val aka: List<String>?
)