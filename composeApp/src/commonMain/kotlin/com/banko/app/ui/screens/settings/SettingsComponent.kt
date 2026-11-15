package com.banko.app.ui.screens.settings

import com.arkivanov.decompose.ComponentContext


class SettingsComponent(
    componentContext: ComponentContext,
    val onNavigateToProfile: () -> Unit = {},
    val onNavigateToBankLinking: () -> Unit = {},
    val onReAuthorize: (institutionId: String) -> Unit = {},
    val openLinkedBanksOnStart: Boolean = false,
): ComponentContext by componentContext