package com.banko.app.ui.screens.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.auth_error_registration_failed
import banko.composeapp.generated.resources.login_email
import banko.composeapp.generated.resources.login_password
import banko.composeapp.generated.resources.register_button
import banko.composeapp.generated.resources.register_consent
import banko.composeapp.generated.resources.register_full_name
import banko.composeapp.generated.resources.register_has_account
import banko.composeapp.generated.resources.register_login_link
import banko.composeapp.generated.resources.register_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun RegisterScreen(component: RegisterComponent) {
    val viewModel = koinViewModel<RegisterViewModel>()
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = stringResource(Res.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = state.fullName,
            onValueChange = viewModel::onFullNameChanged,
            label = { Text(stringResource(Res.string.register_full_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text(stringResource(Res.string.login_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text(stringResource(Res.string.login_password)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(
                checked = state.consentGiven,
                onCheckedChange = viewModel::onConsentChanged,
                enabled = !state.isLoading,
            )
            Text(stringResource(Res.string.register_consent))
        }
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.auth_error_registration_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::register,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            } else {
                Text(stringResource(Res.string.register_button))
            }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = component.onNavigateToLogin) {
            Text(stringResource(Res.string.register_has_account))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(Res.string.register_login_link), color = MaterialTheme.colorScheme.primary)
        }
    }
}

private val PasswordVisualTransformation = VisualTransformation { text ->
    TransformedText(AnnotatedString("\u2022".repeat(text.text.length)), OffsetMapping.Identity)
}
