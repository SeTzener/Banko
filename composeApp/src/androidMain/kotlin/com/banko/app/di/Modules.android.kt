package com.banko.app.di

import com.banko.app.createDataStore
import org.koin.dsl.module

actual val platformModule = module {
        single { createDataStore(context = get()) }
        // add the class that needs to be injected
//        Example 1
//        singleOf(::MyRepositoryImpl).bind<MyRepository>()

//        Example 2 (import the viewModel before to add the ::)
//        viewModelOf(::MyViewModel)
}