package com.banko.app.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.banko.app.database.Entities.CreditorAccount
import com.banko.app.database.Entities.DebtorAccount
import com.banko.app.database.Entities.ExpenseTag
import com.banko.app.database.Entities.Transaction

@Database(
    entities = [
        Transaction::class,
        ExpenseTag::class,
        CreditorAccount::class,
        DebtorAccount::class
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(BankoDatabaseConstructor::class)
@TypeConverters(TypeConverter::class)
abstract class BankoDatabase : RoomDatabase() {
    abstract fun bankoDao(): BankoDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object BankoDatabaseConstructor : RoomDatabaseConstructor<BankoDatabase> {
    override fun initialize(): BankoDatabase
}