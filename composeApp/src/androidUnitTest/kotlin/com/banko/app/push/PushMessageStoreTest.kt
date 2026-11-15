package com.banko.app.push

import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushMessageStoreTest {

    private val values = mutableMapOf<String, Any>()
    private val settings = mockk<Settings>(relaxed = true) {
        every { getStringOrNull(any()) } answers { values[firstArg()] as? String }
        every { putString(any(), any()) } answers { values[firstArg()] = secondArg<String>() }
        every { getBoolean(any(), any()) } answers { values[firstArg()] as? Boolean ?: secondArg() }
        every { putBoolean(any(), any()) } answers { values[firstArg()] = secondArg<Boolean>() }
        every { remove(any()) } answers { values.remove(firstArg()) }
    }
    private val pushStorage = PushStorage(settings)

    @Test
    fun `publishes a known message type`() {
        val store = PushMessageStore(pushStorage)

        store.publishType("eua_expiry_warning")

        assertEquals(PushMessage(PushMessageType.EuaExpiryWarning), store.message.value)
    }

    @Test
    fun `ignores an unknown message type`() {
        val store = PushMessageStore(pushStorage)

        store.publishType("something_else")

        assertNull(store.message.value)
        assertNull(pushStorage.pendingMessageType)
    }

    @Test
    fun `dismiss clears the persisted message`() {
        val store = PushMessageStore(pushStorage)
        store.publishType("eua_expiry_expired")

        store.dismiss()

        assertNull(store.message.value)
        assertNull(pushStorage.pendingMessageType)
    }

    @Test
    fun `restores a message persisted before the store existed`() {
        pushStorage.pendingMessageType = "eua_expiry_expired"

        val store = PushMessageStore(pushStorage)

        assertEquals(PushMessage(PushMessageType.EuaExpiryExpired), store.message.value)
    }
}
