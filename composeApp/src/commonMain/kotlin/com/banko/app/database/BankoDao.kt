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
    @Query(
        """
    SELECT transactions.*, creditor_account.*, debtor_account.*, expense_tag.*
    FROM transactions
    LEFT JOIN creditor_account ON transactions.creditorAccountId = creditor_account.id
    LEFT JOIN debtor_account ON transactions.debtorAccountId = debtor_account.id
    LEFT JOIN expense_tag ON transactions.expenseTagId = expense_tag.id
    ORDER BY transactions.bookingDate DESC
    LIMIT :limit
"""
    )
    fun getAllTransactions(limit: Int): Flow<List<FullTransaction>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getRawTransactionById(transactionId: String): DaoTransaction?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Long

    @Upsert
    suspend fun upsertTransaction(transaction: DaoTransaction)

    // Creditor Accounts
    @Query("SELECT * FROM creditor_account WHERE id = :creditorAccountId")
    fun getCreditorAccountById(creditorAccountId: String): Flow<DaoCreditorAccount?>

    @Upsert
    suspend fun upsertCreditorAccount(creditorAccount: DaoCreditorAccount)

    // Debtor Accounts
    @Query("SELECT * FROM debtor_account WHERE id = :debtorAccountId")
    fun getDebtorAccountById(debtorAccountId: String): Flow<DaoDebtorAccount?>

    @Upsert
    suspend fun upsertDebtorAccount(debtorAccount: DaoDebtorAccount)

    // Expense Tags
    @Query("SELECT * FROM expense_tag WHERE id = :expenseTagId")
    fun getExpenseTagById(expenseTagId: String): Flow<DaoExpenseTag?>

    @Query("SELECT * FROM expense_tag")
    fun getAllExpenseTags(): Flow<List<DaoExpenseTag?>>

    @Upsert
    suspend fun upsertExpenseTag(expenseTag: DaoExpenseTag)

    @Delete
    suspend fun deleteExpenseTag(expenseTag: DaoExpenseTag)

}