package com.banko.app.database.Entities

import androidx.room.Embedded

data class FullTransaction(
    @Embedded val transaction: Transaction,

    @Embedded(prefix = "creditor_") val creditorAccount: CreditorAccount? = null,
    @Embedded(prefix = "debtor_") val debtorAccount: DebtorAccount? = null,
    @Embedded(prefix = "expense_") val expenseTag: ExpenseTag? = null
)