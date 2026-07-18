package com.wealthwise.app.data.local.dao

import androidx.room.*
import com.wealthwise.app.data.local.entity.RawSmsEntity
import com.wealthwise.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRawSms(sms: RawSmsEntity): Long

    @Query("SELECT messageHash FROM raw_sms")
    suspend fun getAllKnownHashes(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'INCOME' AND date BETWEEN :from AND :to
        """
    )
    fun observeIncomeBetween(from: Long, to: Long): Flow<Double>

    // NOTE: category groups are resolved in the mapper layer; this simplified query
    // treats EXPENSE-group categories via the `type` column set during classification.
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE' AND date BETWEEN :from AND :to
        """
    )
    fun observeExpenseBetween(from: Long, to: Long): Flow<Double>

    @Query(
        """
        SELECT category as categoryName, COALESCE(SUM(amount),0) as total
        FROM transactions
        WHERE type = 'EXPENSE' AND date BETWEEN :from AND :to
        GROUP BY category
        ORDER BY total DESC
        """
    )
    fun observeExpenseByCategory(from: Long, to: Long): Flow<List<CategoryTotal>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE merchant LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR bank LIKE '%' || :query || '%'
           OR transactionId LIKE '%' || :query || '%'
        ORDER BY date DESC
        """
    )
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE isRecurring = 1 AND date >= :sinceMillis
        ORDER BY date DESC
        """
    )
    fun observeRecurring(sinceMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDuplicateSuspect = 1 ORDER BY date DESC")
    fun observeDuplicateSuspects(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE merchant = :merchant AND ABS(amount - :amount) < 0.01
          AND date BETWEEN :fromMillis AND :toMillis
        """
    )
    suspend fun findPossibleDuplicates(merchant: String, amount: Double, fromMillis: Long, toMillis: Long): List<TransactionEntity>
}

data class CategoryTotal(val categoryName: String, val total: Double)
