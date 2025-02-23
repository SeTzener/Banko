package com.banko.app.di

import androidx.room.RoomDatabase
import com.banko.app.createDataStore
import com.banko.app.database.BankoDatabase
import com.banko.app.database.databaseBuilder
import org.koin.dsl.module

// Always remember to add the module to Modules.android.kt and Modules.ios.kt
actual val platformModule = module {
    single { createDataStore() }
    single<RoomDatabase.Builder<BankoDatabase>> { databaseBuilder() }
//    Example 1
//    singleOf(::MyRepositoryImpl)

//    Example 2 (import the viewModel before to add the ::)
//    viewModelOf(::MyViewModel)
}