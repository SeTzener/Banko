package com.banko.app.database.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseTag::class,
            parentColumns = ["id"],
            childColumns = ["expenseTagId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CreditorAccount::class,
            parentColumns = ["id"],
            childColumns = ["creditorAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = DebtorAccount::class,
            parentColumns = ["id"],
            childColumns = ["debtorAccountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val bookingDate: String,
    val valueDate: String,
    val amount: String,
    val currency: String,
    val debtorAccountId: String?,
    val remittanceInformationUnstructured: String,
    val remittanceInformationUnstructuredArray: List<String>,
    val bankTransactionCode: String,
    val internalTransactionId: String,
    val creditorName: String?,
    val creditorAccountId: String?,
    val debtorName: String?,
    val remittanceInformationStructuredArray: List<String>?,
    val expenseTagId: String?
)