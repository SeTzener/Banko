package com.banko.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banko.app.database.BankoDatabase
import com.banko.app.domain.model.ExpenseTag
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
class ExpenseTagLocalDataSourceTest {

    private lateinit var dataSource: ExpenseTagLocalDataSource
    private lateinit var db: BankoDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BankoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataSource = ExpenseTagLocalDataSource(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun tag(id: String, name: String = "Groceries", isEarning: Boolean = false) =
        ExpenseTag(id = id, name = name, color = 0xFF00FF, isEarning = isEarning, aka = emptyList())

    @Test
    fun `should return empty list when no expense tags exist`() = runBlocking {
        val tags = dataSource.getAllExpenseTags().first()
        assertEquals(0, tags.size)
    }

    @Test
    fun `should upsert and return a single expense tag`() = runBlocking {
        val tag = tag("tag-1")
        dataSource.upsertExpenseTag(tag)

        val tags = dataSource.getAllExpenseTags().first()
        assertEquals(1, tags.size)
        assertEquals(tag, tags[0])
    }

    @Test
    fun `should upsert and return multiple expense tags`() = runBlocking {
        dataSource.upsertExpenseTag(tag("tag-1"))
        dataSource.upsertExpenseTag(tag("tag-2", isEarning = true))

        val tags = dataSource.getAllExpenseTags().first()
        assertEquals(2, tags.size)
    }

    @Test
    fun `should update existing tag when upserted with same id`() = runBlocking {
        dataSource.upsertExpenseTag(tag("tag-1", name = "Groceries"))
        dataSource.upsertExpenseTag(tag("tag-1", name = "Food"))

        val tags = dataSource.getAllExpenseTags().first()
        assertEquals(1, tags.size)
        assertEquals("Food", tags[0].name)
        assertEquals(0xFF00FF, tags[0].color)
        assertEquals(false, tags[0].isEarning)
        assertEquals(emptyList(), tags[0].aka)
    }

    @Test
    fun `should find expense tag by id`() = runBlocking {
        val tag = tag("tag-1")
        dataSource.upsertExpenseTag(tag)

        val loaded = dataSource.getExpenseTagById("tag-1")
        assertNotNull(loaded)
        assertEquals(tag, loaded)
    }

    @Test
    fun `should return null when finding non-existent id`() = runBlocking {
        val result = dataSource.getExpenseTagById("non-existent")
        assertNull(result)
    }

    @Test
    fun `should delete tag via dao when delete method called`() = runBlocking {
        dataSource.upsertExpenseTag(tag("tag-1"))
        assertEquals(1, dataSource.getAllExpenseTags().first().size)

        dataSource.deleteExpenseTag("tag-1")

        assertEquals(0, dataSource.getAllExpenseTags().first().size)
    }
}
