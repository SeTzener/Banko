package com.banko.app.ui.utils

import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.expense_tag_Other
import org.jetbrains.compose.resources.stringResource

data class UserFacingError(
    val userMessage: String,
    val fullError: String,
)

fun toUserFacingErrorMessage(message: String?): UserFacingError {
    val msg = message ?: "Unknown error"
    return when {
        msg.contains("HttpError(code=404") -> UserFacingError(
            userMessage = "The requested resource was not found.",
            fullError = msg
        )
        msg.contains("HttpError(code=409") -> UserFacingError(
            userMessage = "This item already exists.",
            fullError = msg
        )
        msg.contains("HttpError") -> UserFacingError(
            userMessage = "A server error occurred. Please try again.",
            fullError = msg
        )
        msg.contains("Network") || msg.contains("UnresolvedAddressException") -> UserFacingError(
            userMessage = "No internet connection. Check your connection and try again.",
            fullError = msg
        )
        msg.contains("Failed to assign expense tag") -> UserFacingError(
            userMessage = "Could not save the selected tag. Please try again.",
            fullError = msg
        )
        msg.contains("Failed to") && msg.contains("expense tag") -> UserFacingError(
            userMessage = "Could not update your tags. Please try again.",
            fullError = msg
        )
        else -> UserFacingError(
            userMessage = "Something unexpected happened. Please try again.",
            fullError = msg
        )
    }
}
