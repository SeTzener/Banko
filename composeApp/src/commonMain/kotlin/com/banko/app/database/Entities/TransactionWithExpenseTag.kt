package com.banko.app.database.Entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithExpenseTag(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "expenseTagId",
        entityColumn = "id"
    )
    val expenseTag: ExpenseTag?
)