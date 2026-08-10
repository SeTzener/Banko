package com.banko.app.database.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debtor_account")
data class DebtorAccount(
    @PrimaryKey
    val id: String,
    val iban: String,
    val bban: String
)