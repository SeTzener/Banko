package com.banko.app.ui.screens.banklinking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.account_linked_successfully
import banko.composeapp.generated.resources.account_linking_failed
import banko.composeapp.generated.resources.back
import banko.composeapp.generated.resources.bank_auth_error
import banko.composeapp.generated.resources.bank_auth_error_description
import banko.composeapp.generated.resources.done
import banko.composeapp.generated.resources.iban_label
import banko.composeapp.generated.resources.linked_accounts
import banko.composeapp.generated.resources.linking_account
import banko.composeapp.generated.resources.no_banks_found
import banko.composeapp.generated.resources.retry
import banko.composeapp.generated.resources.search_banks
import banko.composeapp.generated.resources.search_countries
import banko.composeapp.generated.resources.select_bank
import banko.composeapp.generated.resources.select_country
import banko.composeapp.generated.resources.select_country_description
import banko.composeapp.generated.resources.select_bank_description
import com.banko.app.ui.components.BankLinkingWebView
import com.banko.app.ui.components.BankLogo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun BankLinkingScreen(
    component: com.banko.app.ui.screens.banklinking.BankLinkingComponent,
) {
    val viewModel = koinViewModel<BankLinkingViewModel>()
    val screenState by viewModel.screenState.collectAsState()

    val institutionId = component.institutionId
    LaunchedEffect(institutionId) {
        if (institutionId != null && screenState.currentStep == BankLinkingStep.CountrySelection) {
            viewModel.reAuthorize(institutionId)
        }
    }

    val title = when (screenState.currentStep) {
        BankLinkingStep.CountrySelection -> stringResource(Res.string.select_country)
        BankLinkingStep.BankSelection -> stringResource(Res.string.select_bank)
        BankLinkingStep.Authorizing -> screenState.selectedInstitution?.name ?: stringResource(Res.string.select_bank)
        BankLinkingStep.Processing -> stringResource(Res.string.linking_account)
        BankLinkingStep.Success -> stringResource(Res.string.account_linked_successfully)
        BankLinkingStep.Error -> stringResource(Res.string.account_linking_failed)
    }

    val canGoBack = screenState.currentStep in listOf(
        BankLinkingStep.CountrySelection,
        BankLinkingStep.BankSelection,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (screenState.currentStep) {
                BankLinkingStep.CountrySelection -> CountrySelectionStep(
                    onSelectCountry = { viewModel.selectCountry(it) },
                )
                BankLinkingStep.BankSelection -> BankSelectionStep(
                    institutions = screenState.institutions,
                    isLoading = screenState.isLoading,
                    onSelectInstitution = { viewModel.selectInstitution(it) },
                )
                BankLinkingStep.Authorizing -> {
                    val authUrl = screenState.authUrl
                    if (authUrl != null) {
                        BankLinkingWebView(
                            url = authUrl,
                            onRedirectDetected = { url -> viewModel.onWebViewRedirect(url) },
                            onBack = { viewModel.onWebViewBack() },
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                BankLinkingStep.Processing -> ProcessingStep()
                BankLinkingStep.Success -> SuccessStep(
                    linkedAccounts = screenState.linkedAccounts,
                    onDone = { viewModel.done() },
                )
                BankLinkingStep.Error -> ErrorStep(
                    errorMessage = screenState.error,
                    onRetry = { viewModel.retry() },
                    onBack = { component.onGoBack() },
                )
            }

            if (screenState.isLoading && screenState.currentStep != BankLinkingStep.Processing) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun CountrySelectionStep(
    onSelectCountry: (Country) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredPopular = remember(searchQuery) {
        if (searchQuery.isBlank()) popularCountries
        else popularCountries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredAll = remember(searchQuery) {
        if (searchQuery.isBlank()) allCountries
        else allCountries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.select_country_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.search_countries)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (filteredPopular.isNotEmpty()) {
            item {
                Text(
                    text = "Popular",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(filteredPopular) { country ->
                CountryItem(
                    country = country,
                    onClick = { onSelectCountry(country) },
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (filteredAll.isNotEmpty()) {
            item {
                Text(
                    text = "All Countries",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(filteredAll) { country ->
                CountryItem(
                    country = country,
                    onClick = { onSelectCountry(country) },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CountryItem(
    country: Country,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = country.code,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = country.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun BankSelectionStep(
    institutions: List<com.banko.app.api.dto.bankoApi.GoCardlessInstitutionDto>,
    isLoading: Boolean,
    onSelectInstitution: (com.banko.app.api.dto.bankoApi.GoCardlessInstitutionDto) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredInstitutions = remember(searchQuery, institutions) {
        if (searchQuery.isBlank()) institutions
        else institutions.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    if (isLoading && institutions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (institutions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.no_banks_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.select_bank_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.search_banks)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(filteredInstitutions) { institution ->
            BankInstitutionItem(
                institution = institution,
                onClick = { onSelectInstitution(institution) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BankInstitutionItem(
    institution: com.banko.app.api.dto.bankoApi.GoCardlessInstitutionDto,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BankLogo(
            logoUrl = institution.logo,
            bankName = institution.name,
            size = 40,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = institution.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ProcessingStep() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.linking_account),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SuccessStep(
    linkedAccounts: List<com.banko.app.api.dto.bankoApi.LinkedBankAccount>,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.account_linked_successfully),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (linkedAccounts.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.linked_accounts),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))

            linkedAccounts.forEach { account ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        account.accountName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        account.iban?.let { iban ->
                            Text(
                                text = stringResource(Res.string.iban_label, maskIban(iban)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        account.currency?.let { currency ->
                            Text(
                                text = "Currency: $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(Res.string.done),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ErrorStep(
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.bank_auth_error),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.bank_auth_error_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(Res.string.retry),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(Res.string.back),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun maskIban(iban: String): String {
    if (iban.length <= 8) return iban
    val visibleStart = iban.take(4)
    val visibleEnd = iban.takeLast(4)
    val maskedMiddle = "*".repeat(iban.length - 8)
    return "$visibleStart$maskedMiddle$visibleEnd"
}

@Composable
private fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = modifier,
    ) {
        content()
    }
}
