package com.banko.app.ui.screens.settings.bottomsheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import banko.composeapp.generated.resources.Res
import banko.composeapp.generated.resources.error_status
import banko.composeapp.generated.resources.expired
import banko.composeapp.generated.resources.expense_tags_bottom_sheet_button_close
import banko.composeapp.generated.resources.iban_label
import banko.composeapp.generated.resources.linked
import banko.composeapp.generated.resources.linked_banks
import banko.composeapp.generated.resources.no_linked_banks
import banko.composeapp.generated.resources.processing
import banko.composeapp.generated.resources.re_authorize
import com.banko.app.api.dto.bankoApi.BankAuthorizationStatus
import com.banko.app.api.dto.bankoApi.BankAuthDto
import com.banko.app.ui.components.BankLogo
import com.banko.app.ui.screens.settings.SettingsScreenState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedBanksBottomSheetContent(
    screenState: SettingsScreenState,
    onReAuthorize: (String) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.linked_banks),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.expense_tags_bottom_sheet_button_close),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            screenState.isLoadingBanks -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            screenState.bankAuthorizations.isEmpty() -> {
                Text(
                    text = stringResource(Res.string.no_linked_banks),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(screenState.bankAuthorizations) { index, auth ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        BankAuthorizationRow(
                            auth = auth,
                            onReAuthorize = { onReAuthorize(auth.institutionId ?: "") },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BankAuthorizationRow(
    auth: BankAuthDto,
    onReAuthorize: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BankLogo(
                logoUrl = null,
                bankName = auth.institutionName,
                size = 40,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = auth.institutionName ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StatusBadge(status = auth.status)
            }
            if (auth.status == BankAuthorizationStatus.Expired || auth.status == BankAuthorizationStatus.Error) {
                TextButton(onClick = onReAuthorize) {
                    Text(text = stringResource(Res.string.re_authorize))
                }
            }
        }
        if (auth.accounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            auth.accounts.forEach { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 52.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        account.accountName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Row {
                            account.iban?.let { iban ->
                                Text(
                                    text = stringResource(Res.string.iban_label, maskIban(iban)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            account.currency?.let { currency ->
                                Text(
                                    text = " · $currency",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: BankAuthorizationStatus,
) {
    val (text, color) = when (status) {
        BankAuthorizationStatus.Linked -> stringResource(Res.string.linked) to Color(0xFF4CAF50)
        BankAuthorizationStatus.Processing -> stringResource(Res.string.processing) to Color(0xFFFFC107)
        BankAuthorizationStatus.Expired -> stringResource(Res.string.expired) to Color(0xFFF44336)
        BankAuthorizationStatus.Error -> stringResource(Res.string.error_status) to Color(0xFFF44336)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .padding(horizontal = 4.dp),
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(8.dp),
        ) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

private fun maskIban(iban: String): String {
    if (iban.length <= 8) return iban
    val visibleStart = iban.take(4)
    val visibleEnd = iban.takeLast(4)
    val maskedMiddle = "*".repeat(iban.length - 8)
    return "$visibleStart$maskedMiddle$visibleEnd"
}
