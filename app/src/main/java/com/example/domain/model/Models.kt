package com.example.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class BudgetAlertStatus {
    SAFE,       // < 70%
    WARNING,    // 70% - 99%
    OVER_LIMIT  // >= 100%
}

enum class AppNavTab {
    BERANDA,
    DOMPET,
    ANALISIS,
    ANGGARAN,
    KELUARGA
}

data class FamilyMember(
    val id: String,
    val name: String,
    val role: String,
    val colorHex: Long
) {
    val avatarInitial: String
        get() = name.trim().firstOrNull()?.toString()?.uppercase() ?: "?"
}

data class WalletAccount(
    val id: String,
    val name: String,
    val type: String, // "BANK", "CASH", "EWALLET", "SAVINGS"
    val accountNumber: String,
    val balance: Double,
    val colorHex: Long,
    val holderMemberId: String,
    val holderName: String
)

data class CategoryItem(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: Long
)

data class BudgetItem(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val monthlyLimit: Double,
    val currentSpent: Double = 0.0,
    val alertThresholdPercent: Int = 80,
    val monthYear: String = "2026-10"
) {
    val progress: Float
        get() = if (monthlyLimit > 0) (currentSpent / monthlyLimit).toFloat().coerceIn(0f, 1.5f) else 0f

    val percentage: Int
        get() = if (monthlyLimit > 0) ((currentSpent / monthlyLimit) * 100).toInt() else 0

    val remaining: Double
        get() = (monthlyLimit - currentSpent).coerceAtLeast(0.0)

    val alertStatus: BudgetAlertStatus
        get() = when {
            percentage >= 100 -> BudgetAlertStatus.OVER_LIMIT
            percentage >= alertThresholdPercent -> BudgetAlertStatus.WARNING
            else -> BudgetAlertStatus.SAFE
        }
}

data class SavingsGoal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val icon: String = "🎯",
    val colorHex: Long = 0xFF0D9488
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentage: Int
        get() = (progress * 100).toInt()
}

data class TransactionItem(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
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

data class FamilyVaultInfo(
    val id: String = "vault_main",
    val name: String = "Brankas Finansial Keluarga",
    val pairingCode: String = "FAM-8921",
    val memberCount: Int = 4,
    val isSyncing: Boolean = false,
    val lastSyncTime: String = "Baru saja"
)

data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Float,
    val colorHex: Long,
    val iconName: String,
    val transactionCount: Int
)

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val savingsRate: Int
)
