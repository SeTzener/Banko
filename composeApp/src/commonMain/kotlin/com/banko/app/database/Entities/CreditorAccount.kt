package com.banko.app.database.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banko.app.ModelCreditorAccount

@Entity(tableName = "creditor_account")
data class CreditorAccount(
    @PrimaryKey
    val id: String,
    val iban: String,
    val bban: String
)

fun CreditorAccount.toModelItem() = ModelCreditorAccount(
    id = id,
    iban = iban,
    bban = bban
)