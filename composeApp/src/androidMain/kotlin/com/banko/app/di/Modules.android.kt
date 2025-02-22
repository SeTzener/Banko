package com.banko.app.di

import androidx.room.RoomDatabase
import com.banko.app.createDataStore
import com.banko.app.database.BankoDatabase
import com.banko.app.database.databaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
        single { createDataStore(context = get()) }
        single<RoomDatabase.Builder<BankoDatabase>> { databaseBuilder(context = androidContext()) }
        // add the class that needs to be injected
//        Example 1
//        singleOf(::MyRepositoryImpl).bind<MyRepository>()

//        Example 2 (import the viewModel before to add the ::)
//        viewModelOf(::MyViewModel)
}