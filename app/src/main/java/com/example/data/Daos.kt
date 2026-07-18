package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM dettes ORDER BY dateEcheance ASC")
    fun getAllDebts(): Flow<List<Debt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)

    @Query("SELECT * FROM dettes WHERE id = :id")
    suspend fun getDebtById(id: Long): Debt?

    @Delete
    suspend fun deleteDebt(debt: Debt)

    @Query("DELETE FROM dettes WHERE id = :id")
    suspend fun deleteDebtById(id: Long)
}
