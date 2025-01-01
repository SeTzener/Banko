package com.banko.app.api.services

import com.banko.app.api.HttpClientProvider
import com.banko.app.api.dto.nordigen.Accounts
import com.banko.app.api.dto.nordigen.Institutions
import com.banko.app.api.dto.nordigen.Balances
import com.banko.app.api.dto.nordigen.Requisitions
import com.banko.app.api.dto.nordigen.Transactions
import com.banko.app.api.utils.NetworkError
import io.ktor.client.request.header
import com.banko.app.api.utils.Result
import com.banko.app.api.utils.getSafe
import com.banko.app.api.utils.postSafe
import io.ktor.client.request.setBody
import com.banko.config.BuildKonfig
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NordigenApiService
    : KoinComponent {
    private val baseUrl = "https://ob.nordigen.com/api"
    private val version = "/v2"
    private val client by lazy {
        HttpClient(
            HttpClientProvider.invoke(get())
        )
    }
    suspend fun getAccounts(requisitionId: String): Result<Accounts, NetworkError> {
        return client.getSafe<Accounts>("$baseUrl$version/requisitions/$requisitionId") {
            header("Content-Type", "application/json")
        }
    }

    // TODO(): Change country to an enum
    suspend fun getInstitution(country: String): Result<List<Institutions>, NetworkError> {
        return client.getSafe<List<Institutions>>("$baseUrl$version/institutions/?country=$country") {
            header("Content-Type", "application/json")
        }
    }

    // TODO(): Change userLanguage to an enum
    suspend fun getRequisitions(
        institutionId: String,
        userLanguage: String
    ): Result<Requisitions, NetworkError> {
        return client.postSafe<Requisitions>("$baseUrl$version/requisitions/") {
            header("Content-Type", "application/json")
            setBody(
                mapOf(
                    "redirect" to "http://www.yourwebpage.com",
                    "institution_id" to institutionId,
                    "user_language" to userLanguage
                )
            )
        }
    }

    suspend fun getBalances(accountId: String = BuildKonfig.NORDIGEN_ACCOUNT_ID): Result<Balances, NetworkError> {
        return client.getSafe<Balances>("$baseUrl$version/accounts/$accountId/balances/") {
            header("Content-Type", "application/json")
        }
    }

    suspend fun getTransactions(
        accountId: String = BuildKonfig.NORDIGEN_ACCOUNT_ID
    ): Result<Transactions, NetworkError> {
        return client.getSafe<Transactions>("$baseUrl$version/accounts/${accountId}/transactions") {
            header("Content-Type", "application/json")
        }
    }
}