package com.banko.app.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorHandlingTest {

    @Test
    fun `classifyError returns GENERIC for unknown error`() {
        assertEquals(ErrorType.GENERIC, classifyError(Exception("Something went wrong")))
    }

    @Test
    fun `classifyError returns GENERIC for null message`() {
        assertEquals(ErrorType.GENERIC, classifyError(Exception()))
    }

    @Test
    fun `classifyError returns NOT_FOUND for 404 HttpError`() {
        assertEquals(ErrorType.NOT_FOUND, classifyError(Exception("HttpError(code=404, response=...)")))
    }

    @Test
    fun `classifyError returns CONFLICT for 409 HttpError`() {
        assertEquals(ErrorType.CONFLICT, classifyError(Exception("HttpError(code=409, response=...)")))
    }

    @Test
    fun `classifyError returns SERVER_ERROR for other HttpError`() {
        assertEquals(ErrorType.SERVER_ERROR, classifyError(Exception("HttpError(code=500, response=...)")))
    }

    @Test
    fun `classifyError returns NETWORK for network error`() {
        assertEquals(ErrorType.NETWORK, classifyError(Exception("Network request failed")))
    }

    @Test
    fun `classifyError returns NETWORK for UnresolvedAddressException`() {
        assertEquals(ErrorType.NETWORK, classifyError(Exception("UnresolvedAddressException: host not found")))
    }

    @Test
    fun `classifyError returns TAG_ASSIGN_FAILED`() {
        assertEquals(ErrorType.TAG_ASSIGN_FAILED, classifyError(Exception("Failed to assign expense tag")))
    }

    @Test
    fun `classifyError returns TAG_UPDATE_FAILED`() {
        assertEquals(ErrorType.TAG_UPDATE_FAILED, classifyError(Exception("Failed to update expense tag")))
    }
}
