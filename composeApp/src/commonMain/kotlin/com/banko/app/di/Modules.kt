package com.banko.app.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
//    Example 1
//    single {
        // MyRepositoryImpl(get())
//    }//.bind<TokenInterceptor>()

//    Example 2
//    singleOf(::MyRepositoryImpl).bind<MyRepository>()

//    Example 3 (import the viewModel before to add the ::)
//    viewModelOf(::DetailsScreenViewModel)
}

// In the below implementations KoinContext{} is a @Composable function.
// This is necessary when I want to inject a viewModel or anything else in a composable function.
// KoinContext {
//  val viewModel = koinViewModel<DetailsScreenViewModel>()
//  val myRepo = koinInject<MyRepository>()
// )