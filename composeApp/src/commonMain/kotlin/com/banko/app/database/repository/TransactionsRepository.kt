package com.banko.app.database.repository

import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class TransactionsRepository(
    private val bankoDatabase: BankoDatabase,

    ){
    private val dispatchers = Dispatchers.IO

    suspend fun upsertTransaction(transaction: Transaction) {
        bankoDatabase.bankoDao().upsertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        bankoDatabase.bankoDao().deleteTransaction(transaction)
    }
}
