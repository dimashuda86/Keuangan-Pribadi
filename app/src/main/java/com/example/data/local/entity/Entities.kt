package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val colorHex: Long,
    val vaultId: String = "vault_main"
)

@Entity(tableName = "family_groups")
data class FamilyGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pairingCode: String,
    val currency: String = "IDR",
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallets")
data class WalletAccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "BANK", "CASH", "EWALLET", "SAVINGS"
    val accountNumber: String,
    val initialBalance: Double,
    val colorHex: Long,
    val holderMemberId: String,
    val holderName: String
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "EXPENSE", "INCOME", or "TRANSFER"
    val iconName: String,
    val colorHex: Long
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val categoryName: String,
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 80,
    val monthYear: String = "2026-10"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val icon: String = "🎯",
    val colorHex: Long = 0xFF0D9488
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE", "INCOME", or "TRANSFER"
    val categoryId: String,
    val categoryName: String,
    val memberId: String,
    val memberName: String,
    val memberRole: String,
    val walletId: String = "acc_cash",
    val walletName: String = "Dompet Tunai",
    val destinationWalletId: String? = null,
    val destinationWalletName: String? = null,
    val note: String = "",
    val receiptUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val vaultId: String = "vault_main",
    val isSynced: Boolean = true
)
