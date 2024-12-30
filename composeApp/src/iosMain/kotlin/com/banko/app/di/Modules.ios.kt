package com.banko.app.di

import org.koin.dsl.module

// Always remember to add the module to Modules.android.kt and Modules.ios.kt
actual val platformModule = module {
//    Example 1
//    singleOf(::MyRepositoryImpl)

//    Example 2 (import the viewModel before to add the ::)
//    viewModelOf(::MyViewModel)
}