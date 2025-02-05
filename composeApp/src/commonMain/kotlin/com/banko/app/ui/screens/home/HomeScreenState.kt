package com.banko.app.ui.screens.home

import com.banko.app.ui.models.Transaction

data class HomeScreenState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false
)