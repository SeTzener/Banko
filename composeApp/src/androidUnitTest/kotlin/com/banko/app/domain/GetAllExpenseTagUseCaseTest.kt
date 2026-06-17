package com.banko.app.domain

import com.banko.app.database.Entities.ExpenseTag
import com.banko.app.database.repository.ExpenseTagRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class graGetAllExpenseTagUseCaseTest {

    private val expenseTagRepository = mockk<ExpenseTagRepository>()
    private val useCase = GetAllExpenseTagUseCase(expenseTagRepository = expenseTagRepository)

    @Test
    fun `should return all expense tags from repository`() = runBlocking {
        val tags = listOf(
            ExpenseTag(id = "1", name = "Food", color = 0xFF0000, isEarning = false, aka = emptyList()),
            ExpenseTag(id = "2", name = "Transport", color = 0x00FF00, isEarning = false, aka = emptyList())
        )

        every { expenseTagRepository.getAllExpenseTags() } returns flowOf(tags)

        val result = useCase().first()

        assertEquals(tags, result)
    }

    @Test
    fun `should return empty list when no tags exist`() = runBlocking {
        every { expenseTagRepository.getAllExpenseTags() } returns flowOf(emptyList())

        val result = useCase().first()

        assertEquals(emptyList<ExpenseTag>(), result)
    }
}
