package com.banko.app.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.details_button_back
import banko.composeapp.generated.resources.profile
import banko.composeapp.generated.resources.profile_account_id
import banko.composeapp.generated.resources.profile_delete_account
import banko.composeapp.generated.resources.profile_delete_account_confirm
import banko.composeapp.generated.resources.profile_delete_account_confirm_cancel
import banko.composeapp.generated.resources.profile_delete_account_confirm_yes
import banko.composeapp.generated.resources.profile_export_close
import banko.composeapp.generated.resources.profile_export_data
import banko.composeapp.generated.resources.profile_export_title
import banko.composeapp.generated.resources.profile_logout
import banko.composeapp.generated.resources.profile_logout_confirm
import banko.composeapp.generated.resources.profile_logout_confirm_cancel
import banko.composeapp.generated.resources.profile_logout_confirm_yes
import banko.composeapp.generated.resources.profile_re_accept_consent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(component: ProfileComponent) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(Res.string.profile_logout)) },
            text = { Text(stringResource(Res.string.profile_logout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) {
                    Text(stringResource(Res.string.profile_logout_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(Res.string.profile_logout_confirm_cancel))
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.profile_delete_account)) },
            text = { Text(stringResource(Res.string.profile_delete_account_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text(stringResource(Res.string.profile_delete_account_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.profile_delete_account_confirm_cancel))
                }
            },
        )
    }

    if (state.exportData != null) {
        val export = state.exportData!!
        AlertDialog(
            onDismissRequest = { viewModel.clearExportData() },
            title = { Text(stringResource(Res.string.profile_export_title)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Email: ${export.email}")
                    Text("Full Name: ${export.fullName ?: "N/A"}")
                    Text("Phone: ${export.phoneNumber ?: "N/A"}")
                    Text("Address: ${export.address ?: "N/A"}")
                    Text("Consent Given: ${export.consentGiven}")
                    Text("Created: ${export.createdAt}")
                    export.consentLogs.forEach { log ->
                        Text("- ${log.policyTitle} (${log.policyVersion}): ${if (log.accepted) "accepted" else "not accepted"} at ${log.recordedAt}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearExportData() }) {
                    Text(stringResource(Res.string.profile_export_close))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = component::goBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.details_button_back))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp).verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = state.accountId ?: "",
                onValueChange = {},
                label = { Text(stringResource(Res.string.profile_account_id)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (state.profile != null) {
                val profile = state.profile!!
                Spacer(Modifier.height(8.dp))
                Text("Email: ${profile.email}", style = MaterialTheme.typography.bodyMedium)
                profile.fullName?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Name: $it", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.exportData() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Text(stringResource(Res.string.profile_export_data))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.acceptConsent("00000000-0000-0000-0000-000000000001") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Text(stringResource(Res.string.profile_re_accept_consent))
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(Res.string.profile_logout))
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Text(
                    stringResource(Res.string.profile_delete_account),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
