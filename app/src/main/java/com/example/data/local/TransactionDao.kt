package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM bank_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM bank_transactions WHERE currency = :currency ORDER BY timestamp DESC")
    fun getTransactionsByCurrency(currency: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM bank_transactions WHERE status = :status ORDER BY timestamp DESC")
    fun getTransactionsByStatus(status: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM bank_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM bank_transactions 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR recipient LIKE '%' || :query || '%' 
           OR referenceNumber LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT COUNT(*) FROM bank_transactions")
    suspend fun getTransactionCount(): Int

    @Query("DELETE FROM bank_transactions")
    suspend fun clearAll()
}
