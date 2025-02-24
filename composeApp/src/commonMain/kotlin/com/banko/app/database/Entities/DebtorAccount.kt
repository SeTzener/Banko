package com.banko.app.database.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banko.app.ModelDebtorAccount

@Entity(tableName = "debtor_account")
data class DebtorAccount(
    @PrimaryKey
    val id: String,
    val iban: String,
    val bban: String
)

fun DebtorAccount.toModelItem() = ModelDebtorAccount(
    id = id,
    iban = iban,
    bban = bban
)