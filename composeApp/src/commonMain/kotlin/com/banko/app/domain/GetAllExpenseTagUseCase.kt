package com.banko.app.domain

import com.banko.app.DatabaseExpenseTagRepository

class GetAllExpenseTagUseCase(
    private val expenseTagRepository: DatabaseExpenseTagRepository
) {
    operator fun invoke() = expenseTagRepository.getAllExpenseTags()
}