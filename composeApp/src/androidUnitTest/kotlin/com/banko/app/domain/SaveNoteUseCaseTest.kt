package com.banko.app.domain

import com.banko.app.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SaveNoteUseCaseTest {

    private val transactionRepository = mockk<TransactionRepository>()
    private val useCase = SaveNoteUseCase(transactionRepository)

    @Test
    fun `should save note via repository`() = runBlocking {
        val id = "tx-1"
        val note = "Test note"

        coEvery { transactionRepository.saveNote(id, note) } returns Unit

        useCase(id, note)

        coVerify { transactionRepository.saveNote(id, note) }
    }

    @Test
    fun `should propagate exception from repository`() = runBlocking {
        val id = "tx-1"
        val note = "Test note"

        coEvery { transactionRepository.saveNote(id, note) } throws RuntimeException("Network error")

        val exception = runCatching { useCase(id, note) }.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is RuntimeException)
    }
}
