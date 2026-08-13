package com.banko.app.domain

import com.banko.app.data.repository.ExpenseTagRepository

class GetAllExpenseTagUseCase(
    private val expenseTagRepository: ExpenseTagRepository
) {
    operator fun invoke() = expenseTagRepository.getAllExpenseTags()
}
