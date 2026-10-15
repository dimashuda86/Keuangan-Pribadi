package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyGroupEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getTransactionsByMember(memberId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getTransactionsByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId OR destinationWalletId = :walletId ORDER BY timestamp DESC")
    fun getTransactionsByWallet(walletId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("UPDATE transactions SET memberName = :newName, memberRole = :newRole WHERE memberId = :memberId")
    suspend fun updateMemberNameInTransactions(memberId: String, newName: String, newRole: String)
}

@Dao
interface WalletAccountDao {
    @Query("SELECT * FROM wallets ORDER BY name ASC")
    fun getAllWallets(): Flow<List<WalletAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletAccountEntity)

    @Update
    suspend fun updateWallet(wallet: WalletAccountEntity)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteWalletById(id: String)

    @Query("UPDATE wallets SET initialBalance = 0")
    suspend fun resetWalletBalances()

    @Query("DELETE FROM wallets")
    suspend fun clearAllWallets()

    @Query("UPDATE wallets SET holderName = :newName WHERE holderMemberId = :memberId")
    suspend fun updateMemberNameInWallets(memberId: String, newName: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: String)

    @Query("DELETE FROM budgets")
    suspend fun clearAllBudgets()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)

    @Query("DELETE FROM savings_goals")
    suspend fun clearAllGoals()
}

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM users")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getMemberCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<FamilyMemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMemberEntity)

    @Update
    suspend fun updateMember(member: FamilyMemberEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteMemberById(id: String)
}

@Dao
interface FamilyGroupDao {
    @Query("SELECT * FROM family_groups LIMIT 1")
    fun getFamilyGroup(): Flow<FamilyGroupEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyGroup(group: FamilyGroupEntity)
}
