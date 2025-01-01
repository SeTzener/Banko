package com.banko.app.di

import com.banko.app.createDataStore
import org.koin.dsl.module

// Always remember to add the module to Modules.android.kt and Modules.ios.kt
actual val platformModule = module {
    single { createDataStore() }
//    Example 1
//    singleOf(::MyRepositoryImpl)

//    Example 2 (import the viewModel before to add the ::)
//    viewModelOf(::MyViewModel)
}