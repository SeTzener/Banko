package com.banko.app.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.banko.app.database.Entities.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BankoDao {
    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<Transaction?>>

    @Upsert
    suspend fun upsertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

}