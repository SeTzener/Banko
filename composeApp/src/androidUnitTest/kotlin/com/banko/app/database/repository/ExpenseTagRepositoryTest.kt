package com.banko.app.database.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.database.BankoDatabase
import com.banko.app.database.Entities.ExpenseTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExpenseTagRepositoryTest {

    private lateinit var repo: ExpenseTagRepository
    private lateinit var db: BankoDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = ExpenseTagRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should return empty list when no expense tags exist`() = runBlocking {
        val tags = repo.getAllExpenseTags().first()
        assertEquals(0, tags.size)
    }

    @Test
    fun `should upsert and return a single expense tag`() = runBlocking {
        val tag = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        repo.upsertExpenseTag(tag)

        val tags = repo.getAllExpenseTags().first()
        assertEquals(1, tags.size)
        assertEquals(tag, tags[0])
    }

    @Test
    fun `should upsert and return multiple expense tags`() = runBlocking {
        repo.upsertExpenseTag(
            ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        )
        repo.upsertExpenseTag(
            ExpenseTag(id = "tag-2", name = "Salary", color = 0x00FF00, isEarning = true, aka = null)
        )

        val tags = repo.getAllExpenseTags().first()
        assertEquals(2, tags.size)
    }

    @Test
    fun `should update existing tag when upserted with same id`() = runBlocking {
        repo.upsertExpenseTag(
            ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        )
        repo.upsertExpenseTag(
            ExpenseTag(id = "tag-1", name = "Food", color = 0xFF00FF, isEarning = false, aka = null)
        )

        val tags = repo.getAllExpenseTags().first()
        assertEquals(1, tags.size)
        val updated = tags[0]
        assertEquals("Food", updated?.name)
        assertEquals(0xFF00FF, updated?.color)
        assertEquals(false, updated?.isEarning)
        assertEquals(null, updated?.aka)
    }

    @Test
    fun `should find expense tag by id`() = runBlocking {
        val tag = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        repo.upsertExpenseTag(tag)

        val loaded = repo.findExpenseTagById("tag-1").first()
        assertNotNull(loaded)
        assertEquals(tag, loaded)
    }

    @Test
    fun `should return null when finding non-existent id`() = runBlocking {
        val result = repo.findExpenseTagById("non-existent").first()
        assertNull(result)
    }

    @Test
    fun `should return null when finding null id`() = runBlocking {
        val result = repo.findExpenseTagById(null).first()
        assertNull(result)
    }

    @Test
    fun `should delete tag via dao when repro delete method called`() = runBlocking {
        val tag = ExpenseTag(id = "tag-1", name = "Groceries", color = 0xFF00FF, isEarning = false, aka = null)
        repo.upsertExpenseTag(tag)
        assertEquals(1, db.bankoDao().getAllExpenseTags().first().size)

        db.bankoDao().deleteExpenseTag(tag)

        assertEquals(0, db.bankoDao().getAllExpenseTags().first().size)
    }
}
