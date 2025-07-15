package com.banko.app.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.banko.app.DaoCreditorAccount
import com.banko.app.DaoDebtorAccount
import com.banko.app.DaoExpenseTag
import com.banko.app.DaoTransaction
import com.banko.app.database.Entities.FullTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface BankoDao {

    // Transactions
    @Query(
        """
        SELECT 
            transactions.*,
            creditor_account.id AS creditor_id, creditor_account.iban AS creditor_iban, creditor_account.bban AS creditor_bban,
            debtor_account.id AS debtor_id, debtor_account.iban AS debtor_iban, debtor_account.bban AS debtor_bban,
            expense_tag.id AS expense_id, expense_tag.color AS expense_color, expense_tag.name AS expense_name, 
            expense_tag.aka AS expense_aka, expense_tag.isEarning AS expense_isEarning
        FROM transactions
        LEFT JOIN creditor_account ON transactions.creditorAccountId = creditor_account.id
        LEFT JOIN debtor_account ON transactions.debtorAccountId = debtor_account.id
        LEFT JOIN expense_tag ON transactions.expenseTagId = expense_tag.id
        ORDER BY transactions.bookingDate DESC
        LIMIT :limit
        """
    )
    fun getTransactionsPagingSource(limit: Int): Flow<List<FullTransaction>>

    @Query(
        """
            SELECT BookingDate FROM transactions
            ORDER BY BookingDate ASC LIMIT 1
        """
    )
    suspend fun getOldestTransactions(): String?

    @Transaction
    @Query(
        """
            SELECT 
            transactions.*,
            creditor_account.id AS creditor_id, creditor_account.iban AS creditor_iban, creditor_account.bban AS creditor_bban,
            debtor_account.id AS debtor_id, debtor_account.iban AS debtor_iban, debtor_account.bban AS debtor_bban,
            expense_tag.id AS expense_id, expense_tag.color AS expense_color, expense_tag.name AS expense_name, 
            expense_tag.aka AS expense_aka, expense_tag.isEarning AS expense_isEarning
        FROM transactions
        LEFT JOIN creditor_account ON transactions.creditorAccountId = creditor_account.id
        LEFT JOIN debtor_account ON transactions.debtorAccountId = debtor_account.id
        LEFT JOIN expense_tag ON transactions.expenseTagId = expense_tag.id 
        WHERE transactions.BookingDate BETWEEN :startDate AND :endDate
            """
    )
    fun getTransactionsForMonth(startDate: String, endDate: String): Flow<List<FullTransaction>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getRawTransactionById(transactionId: String): DaoTransaction?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Long

    @Query(
        """
            UPDATE transactions
            SET note = :note
            WHERE id = :id
        """
    )
    suspend fun saveNote(id: String, note: String)

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