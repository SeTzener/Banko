package com.banko.app.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.banko.app.DaoCreditorAccount
import com.banko.app.DaoDebtorAccount
import com.banko.app.DaoExpenseTag
import com.banko.app.DaoTransaction
import com.banko.app.database.Entities.FullTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BankoDao {

    // Transactions
    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<FullTransaction?>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getRawTransactionById(transactionId: String): DaoTransaction?

    @Upsert
    suspend fun upsertTransaction(transaction: DaoTransaction)

    // Creditor Accounts
    @Query("SELECT * FROM creditor_account WHERE id = :creditorAccountId")
    suspend fun getCreditorAccountById(creditorAccountId: String): DaoCreditorAccount?

    @Upsert
    suspend fun upsertCreditorAccount(creditorAccount: DaoCreditorAccount)

    // Debtor Accounts
    @Query("SELECT * FROM debtor_account WHERE id = :debtorAccountId")
    suspend fun getDebtorAccountById(debtorAccountId: String): DaoDebtorAccount?

    @Upsert
    suspend fun upsertDebtorAccount(debtorAccount: DaoDebtorAccount)

    // Expense Tags
    @Query("SELECT * FROM expense_tag WHERE id = :expenseTagId")
    suspend fun getExpenseTagById(expenseTagId: String): DaoExpenseTag?

    @Query("SELECT * FROM expense_tag")
    fun getAllExpenseTags(): Flow<List<DaoExpenseTag?>>

    @Upsert
    suspend fun upsertExpenseTag(expenseTag: DaoExpenseTag)

    @Delete
    suspend fun deleteExpenseTag(expenseTag: DaoExpenseTag)

}