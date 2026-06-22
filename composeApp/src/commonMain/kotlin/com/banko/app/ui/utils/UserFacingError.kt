package com.banko.app.ui.utils

import androidx.compose.runtime.Composable
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.error_conflict
import banko.composeapp.generated.resources.error_generic
import banko.composeapp.generated.resources.error_network
import banko.composeapp.generated.resources.error_not_found
import banko.composeapp.generated.resources.error_server
import banko.composeapp.generated.resources.error_tag_assign
import banko.composeapp.generated.resources.error_tag_update
import org.jetbrains.compose.resources.stringResource

enum class ErrorType {
    NOT_FOUND, CONFLICT, SERVER_ERROR, NETWORK,
    TAG_ASSIGN_FAILED, TAG_UPDATE_FAILED, GENERIC
}

data class ErrorState(
    val type: ErrorType,
    val fullMessage: String? = null,
)

fun classifyError(throwable: Throwable): ErrorType {
    val msg = throwable.message ?: return ErrorType.GENERIC
    return when {
        msg.contains("HttpError(code=404") -> ErrorType.NOT_FOUND
        msg.contains("HttpError(code=409") -> ErrorType.CONFLICT
        msg.contains("HttpError") -> ErrorType.SERVER_ERROR
        msg.contains("Network") || msg.contains("UnresolvedAddressException") -> ErrorType.NETWORK
        msg.contains("Failed to assign expense tag") -> ErrorType.TAG_ASSIGN_FAILED
        msg.contains("Failed to") && msg.contains("expense tag") -> ErrorType.TAG_UPDATE_FAILED
        else -> ErrorType.GENERIC
    }
}

@Composable
fun ErrorType.toUserMessage(): String = when (this) {
    ErrorType.NOT_FOUND -> stringResource(Res.string.error_not_found)
    ErrorType.CONFLICT -> stringResource(Res.string.error_conflict)
    ErrorType.SERVER_ERROR -> stringResource(Res.string.error_server)
    ErrorType.NETWORK -> stringResource(Res.string.error_network)
    ErrorType.TAG_ASSIGN_FAILED -> stringResource(Res.string.error_tag_assign)
    ErrorType.TAG_UPDATE_FAILED -> stringResource(Res.string.error_tag_update)
    ErrorType.GENERIC -> stringResource(Res.string.error_generic)
}
