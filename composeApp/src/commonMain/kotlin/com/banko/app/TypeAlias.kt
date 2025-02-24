package com.banko.app

// Transaction
typealias ModelTransaction = com.banko.app.ui.models.Transaction
typealias DaoTransaction = com.banko.app.database.Entities.Transaction
typealias DtoTransaction = com.banko.app.api.dto.bankoApi.Transaction

// DebtorAccount
typealias ModelDebtorAccount = com.banko.app.ui.models.DebtorAccount
typealias DaoDebtorAccount = com.banko.app.database.Entities.DebtorAccount
typealias DtoDebtorAccount = com.banko.app.api.dto.bankoApi.DebtorAccount

// Creditor Account
typealias ModelCreditorAccount = com.banko.app.ui.models.CreditorAccount
typealias DaoCreditorAccount = com.banko.app.database.Entities.CreditorAccount
typealias DtoCreditorAccount = com.banko.app.api.dto.bankoApi.CreditorAccount

// Expense Tag
typealias ModelExpenseTag = com.banko.app.ui.models.ExpenseTag
typealias DaoExpenseTag = com.banko.app.database.Entities.ExpenseTag
typealias DtoExpenseTag = com.banko.app.api.dto.bankoApi.ExpenseTag

// Repositories
typealias ApiTransasctionRepository = com.banko.app.api.repositories.TransactionsRepository
typealias ApiExpenseTagRepository = com.banko.app.api.repositories.ExpenseTagRepository
typealias DatabaseTransactionRepository = com.banko.app.database.repository.TransactionsRepository
typealias DatabaseExpenseTagRepository = com.banko.app.database.repository.ExpenseTagRepository
