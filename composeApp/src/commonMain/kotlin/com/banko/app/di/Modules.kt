package com.banko.app.di

import com.banko.app.api.services.NordigenTokenProvider
import com.banko.app.api.services.OauthNordigenApi
import com.banko.app.api.services.TokenInterceptor
import com.banko.app.database.BankoDatabase
import com.banko.app.database.repository.TransactionsRepository
import com.banko.app.database.CreateDatabase
import com.banko.app.database.repository.ExpenseTagRepository
import com.banko.app.ui.screens.settings.SettingsScreenViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

expect val platformModule: Module

val  sharedModule = module {
    singleOf(::TokenInterceptor)
    single { TokenInterceptor.Feature(get()) }
    singleOf(::NordigenTokenProvider)
    singleOf(::OauthNordigenApi)
    single<BankoDatabase> { CreateDatabase(get()).getDatabase() }
    singleOf(::TransactionsRepository)
    singleOf(::ExpenseTagRepository)

    // View models
    viewModelOf(::SettingsScreenViewModel)
}

//    Example 1
//    single {
        // MyRepositoryImpl(get())
//    }//.bind<TokenInterceptor>()

//    Example 2
//    singleOf(::MyRepositoryImpl).bind<MyRepository>()

//    Example 3 (import the viewModel before to add the ::)
//    viewModelOf(::DetailsScreenViewModel)

// In the below implementations KoinContext{} is a @Composable function.
// This is necessary when I want to inject a viewModel or anything else in a composable function.
// KoinContext {
//  val viewModel = koinViewModel<DetailsScreenViewModel>()
//  val myRepo = koinInject<MyRepository>()
// )