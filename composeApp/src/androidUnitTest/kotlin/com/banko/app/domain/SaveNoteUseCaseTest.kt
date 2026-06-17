package com.banko.app.domain

import com.banko.app.api.repositories.TransactionsRepository as ApiTransactionsRepo
import com.banko.app.api.utils.Result
import com.banko.app.database.repository.TransactionsRepository as DatabaseTransactionsRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SaveNoteUseCaseTest {

    private val apiTransactionsRepository = mockk<ApiTransactionsRepo>()
    private val transactionRepository = mockk<DatabaseTransactionsRepo>()
    private val useCase = SaveNoteUseCase(
        apiTransactionsRepository = apiTransactionsRepository,
        transactionRepository = transactionRepository
    )

    @Test
    fun `should save note locally when API succeeds`() = runBlocking {
        val id = "tx-1"
        val note = "Test note"

        coEvery { apiTransactionsRepository.saveNote(id, note) } returns Result.Success("ok")
        coEvery { transactionRepository.saveNote(id, note) } returns Unit

        useCase(id, note)

        coVerify {
            apiTransactionsRepository.saveNote(id, note)
            transactionRepository.saveNote(id, note)
        }
    }

    @Test
    fun `should not save note locally when API fails`() = runBlocking {
        val id = "tx-1"
        val note = "Test note"

        coEvery { apiTransactionsRepository.saveNote(id, note) } returns Result.Error.HttpError(500, "Server error")

        useCase(id, note)

        coVerify {
            apiTransactionsRepository.saveNote(id, note)
        }
        coVerify(exactly = 0) {
            transactionRepository.saveNote(any(), any())
        }
    }

    @Test
    fun `should handle exception from API gracefully`() = runBlocking {
        val id = "tx-1"
        val note = "Test note"

        coEvery { apiTransactionsRepository.saveNote(id, note) } throws RuntimeException("Network error")

        useCase(id, note)

        coVerify {
            apiTransactionsRepository.saveNote(id, note)
        }
        coVerify(exactly = 0) {
            transactionRepository.saveNote(any(), any())
        }
    }
}
