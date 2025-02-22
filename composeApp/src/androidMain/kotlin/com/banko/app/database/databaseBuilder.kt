package com.banko.app.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun databaseBuilder(context: Context): RoomDatabase.Builder<BankoDatabase> {
    val dbFile = context.getDatabasePath("banko.db")
    return Room.databaseBuilder(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}