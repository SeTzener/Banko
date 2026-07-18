package com.banko.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect @Composable
fun BankLinkingWebView(
    url: String,
    onRedirectDetected: (url: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
)
