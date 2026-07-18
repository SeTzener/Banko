package com.banko.app.ui.screens.settings

import com.arkivanov.decompose.ComponentContext
import org.koin.compose.viewmodel.koinViewModel


class SettingsComponent(
    componentContext: ComponentContext,
    val onNavigateToProfile: () -> Unit = {},
    val onNavigateToBankLinking: () -> Unit = {},
    val onReAuthorize: (institutionId: String) -> Unit = {},
): ComponentContext by componentContext