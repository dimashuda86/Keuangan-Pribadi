package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyGroupEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletAccountEntity
import com.example.domain.model.BudgetItem
import com.example.domain.model.CategoryItem
import com.example.domain.model.CategorySpending
import com.example.domain.model.FamilyMember
import com.example.domain.model.FamilyVaultInfo
import com.example.domain.model.MonthlySummary
import com.example.domain.model.SavingsGoal
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.WalletAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FamilyFinanceRepository(private val database: AppDatabase) {

    val transactions: Flow<List<TransactionItem>> =
        database.transactionDao().getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }

    val categories: Flow<List<CategoryItem>> =
        database.categoryDao().getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }

    val familyMembers: Flow<List<FamilyMember>> =
        database.familyMemberDao().getAllMembers().map { entities ->
            entities.map { it.toDomain() }
        }

    val savingsGoals: Flow<List<SavingsGoal>> =
        database.savingsGoalDao().getAllGoals().map { entities ->
            entities.map { it.toDomain() }
        }

    // Wallets with dynamically computed balances from transactions!
    val wallets: Flow<List<WalletAccount>> =
        combine(
            database.walletAccountDao().getAllWallets(),
            database.transactionDao().getAllTransactions()
        ) { walletEntities, txList ->
            walletEntities.map { entity ->
                var computedBalance = entity.initialBalance
                txList.forEach { tx ->
                    if (tx.walletId == entity.id) {
                        when (tx.type) {
                            "EXPENSE" -> computedBalance -= tx.amount
                            "INCOME" -> computedBalance += tx.amount
                            "TRANSFER" -> computedBalance -= tx.amount
                        }
                    }
                    if (tx.destinationWalletId == entity.id && tx.type == "TRANSFER") {
                        computedBalance += tx.amount
                    }
                }
                WalletAccount(
                    id = entity.id,
                    name = entity.name,
                    type = entity.type,
                    accountNumber = entity.accountNumber,
                    balance = computedBalance,
                    colorHex = entity.colorHex,
                    holderMemberId = entity.holderMemberId,
                    holderName = entity.holderName
                )
            }
        }

    val familyVault: Flow<FamilyVaultInfo> =
        combine(
            database.familyGroupDao().getFamilyGroup(),
            database.familyMemberDao().getAllMembers()
        ) { group, members ->
            FamilyVaultInfo(
                id = group?.id ?: "vault_main",
                name = group?.name ?: "Brankas Finansial Keluarga",
                pairingCode = group?.pairingCode ?: "FAM-8921",
                memberCount = members.size.coerceAtLeast(1),
                isSyncing = false,
                lastSyncTime = "Tersinkronisasi Realtime"
            )
        }

    // Combines budgets with current actual spending from transactions
    val budgetsWithSpending: Flow<List<BudgetItem>> =
        combine(
            database.budgetDao().getAllBudgets(),
            database.transactionDao().getAllTransactions()
        ) { budgetEntities, transactionEntities ->
            val expenseTransactions = transactionEntities.filter { it.type == "EXPENSE" }
            val spendingByCategory = expenseTransactions.groupBy { it.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            budgetEntities.map { entity ->
                val spent = spendingByCategory[entity.categoryId] ?: 0.0
                BudgetItem(
                    id = entity.id,
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    monthlyLimit = entity.monthlyLimit,
                    currentSpent = spent,
                    alertThresholdPercent = entity.alertThresholdPercent,
                    monthYear = entity.monthYear
                )
            }
        }

    // Category Spending breakdown for interactive Donut Chart
    val categorySpendingBreakdown: Flow<List<CategorySpending>> =
        combine(
            database.transactionDao().getAllTransactions(),
            database.categoryDao().getAllCategories()
        ) { transactions, categories ->
            val expenseTransactions = transactions.filter { it.type == "EXPENSE" }
            val totalExpense = expenseTransactions.sumOf { it.amount }
            val categoryMap = categories.associateBy { it.id }

            if (totalExpense <= 0) {
                emptyList()
            } else {
                expenseTransactions.groupBy { it.categoryId }
                    .map { (catId, txList) ->
                        val catTotal = txList.sumOf { it.amount }
                        val cat = categoryMap[catId]
                        CategorySpending(
                            categoryId = catId,
                            categoryName = cat?.name ?: txList.firstOrNull()?.categoryName ?: "Lainnya",
                            totalAmount = catTotal,
                            percentage = (catTotal / totalExpense).toFloat(),
                            colorHex = cat?.colorHex ?: 0xFF0D9488,
                            iconName = cat?.iconName ?: "ShoppingBag",
                            transactionCount = txList.size
                        )
                    }
                    .sortedByDescending { it.totalAmount }
            }
        }

    // Summary calculation (Income, Expense, Balance, Savings Ratio)
    val monthlySummary: Flow<MonthlySummary> =
        combine(
            database.transactionDao().getAllTransactions(),
            wallets
        ) { transactions, walletList ->
            val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val totalAssets = walletList.sumOf { it.balance }
            val savingsRate = if (income > 0) (((income - expense) / income) * 100).toInt().coerceIn(0, 100) else 0

            MonthlySummary(
                totalIncome = income,
                totalExpense = expense,
                balance = totalAssets,
                savingsRate = savingsRate
            )
        }

    suspend fun addTransaction(transaction: TransactionItem): Long {
        return database.transactionDao().insertTransaction(transaction.toEntity())
    }

    suspend fun deleteTransaction(id: Long) {
        database.transactionDao().deleteById(id)
    }

    suspend fun saveBudget(budget: BudgetItem) {
        database.budgetDao().insertBudget(
            BudgetEntity(
                id = budget.id,
                categoryId = budget.categoryId,
                categoryName = budget.categoryName,
                monthlyLimit = budget.monthlyLimit,
                alertThresholdPercent = budget.alertThresholdPercent,
                monthYear = budget.monthYear
            )
        )
    }

    suspend fun addWallet(wallet: WalletAccount) {
        database.walletAccountDao().insertWallet(
            WalletAccountEntity(
                id = wallet.id,
                name = wallet.name,
                type = wallet.type,
                accountNumber = wallet.accountNumber,
                initialBalance = wallet.balance,
                colorHex = wallet.colorHex,
                holderMemberId = wallet.holderMemberId,
                holderName = wallet.holderName
            )
        )
    }

    suspend fun addSavingsGoal(goal: SavingsGoal) {
        database.savingsGoalDao().insertGoal(
            SavingsGoalEntity(
                id = goal.id,
                title = goal.title,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                targetDate = goal.targetDate,
                icon = goal.icon,
                colorHex = goal.colorHex
            )
        )
    }

    suspend fun updateWallet(wallet: WalletAccount) {
        database.walletAccountDao().insertWallet(
            WalletAccountEntity(
                id = wallet.id,
                name = wallet.name,
                type = wallet.type,
                accountNumber = wallet.accountNumber,
                initialBalance = wallet.balance,
                colorHex = wallet.colorHex,
                holderMemberId = wallet.holderMemberId,
                holderName = wallet.holderName
            )
        )
    }

    suspend fun deleteWallet(id: String) {
        database.walletAccountDao().deleteWalletById(id)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal) {
        database.savingsGoalDao().insertGoal(
            SavingsGoalEntity(
                id = goal.id,
                title = goal.title,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                targetDate = goal.targetDate,
                icon = goal.icon,
                colorHex = goal.colorHex
            )
        )
    }

    suspend fun deleteSavingsGoal(id: String) {
        database.savingsGoalDao().deleteGoalById(id)
    }

    suspend fun updateTransaction(transaction: TransactionItem) {
        database.transactionDao().insertTransaction(transaction.toEntity())
    }

    suspend fun resetAllWalletsToZero() {
        database.walletAccountDao().resetWalletBalances()
    }

    suspend fun resetData(toCleanSlate: Boolean) {
        database.transactionDao().clearAllTransactions()
        if (toCleanSlate) {
            database.savingsGoalDao().clearAllGoals()
            database.budgetDao().clearAllBudgets()
            database.walletAccountDao().resetWalletBalances()
        } else {
            database.budgetDao().clearAllBudgets()
            database.savingsGoalDao().clearAllGoals()
            database.walletAccountDao().clearAllWallets()
            database.seedInitialDataInternal()
        }
    }

    suspend fun updateVaultName(name: String) {
        database.familyGroupDao().insertFamilyGroup(
            FamilyGroupEntity(
                id = "vault_main",
                name = name,
                pairingCode = "FAM-8921",
                currency = "IDR",
                lastSyncTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addFamilyMember(member: FamilyMember) {
        database.familyMemberDao().insertMember(
            FamilyMemberEntity(
                id = member.id,
                name = member.name,
                role = member.role,
                colorHex = member.colorHex,
                vaultId = "vault_main"
            )
        )
    }

    suspend fun updateFamilyMember(member: FamilyMember) {
        database.familyMemberDao().updateMember(
            FamilyMemberEntity(
                id = member.id,
                name = member.name,
                role = member.role,
                colorHex = member.colorHex,
                vaultId = "vault_main"
            )
        )
        database.transactionDao().updateMemberNameInTransactions(member.id, member.name, member.role)
        database.walletAccountDao().updateMemberNameInWallets(member.id, member.name)
    }

    suspend fun deleteFamilyMember(id: String) {
        database.familyMemberDao().deleteMemberById(id)
    }

    // Mapper extensions
    private fun TransactionEntity.toDomain(): TransactionItem = TransactionItem(
        id = id,
        title = title,
        amount = amount,
        type = when (type) {
            "INCOME" -> TransactionType.INCOME
            "TRANSFER" -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        },
        categoryId = categoryId,
        categoryName = categoryName,
        memberId = memberId,
        memberName = memberName,
        memberRole = memberRole,
        walletId = walletId,
        walletName = walletName,
        destinationWalletId = destinationWalletId,
        destinationWalletName = destinationWalletName,
        note = note,
        receiptUri = receiptUri,
        timestamp = timestamp,
        vaultId = vaultId,
        isSynced = isSynced
    )

    private fun TransactionItem.toEntity(): TransactionEntity = TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type.name,
        categoryId = categoryId,
        categoryName = categoryName,
        memberId = memberId,
        memberName = memberName,
        memberRole = memberRole,
        walletId = walletId,
        walletName = walletName,
        destinationWalletId = destinationWalletId,
        destinationWalletName = destinationWalletName,
        note = note,
        receiptUri = receiptUri,
        timestamp = timestamp,
        vaultId = vaultId,
        isSynced = isSynced
    )

    private fun CategoryEntity.toDomain(): CategoryItem = CategoryItem(
        id = id,
        name = name,
        type = when (type) {
            "INCOME" -> TransactionType.INCOME
            "TRANSFER" -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        },
        iconName = iconName,
        colorHex = colorHex
    )

    private fun FamilyMemberEntity.toDomain(): FamilyMember = FamilyMember(
        id = id,
        name = name,
        role = role,
        colorHex = colorHex
    )

    private fun SavingsGoalEntity.toDomain(): SavingsGoal = SavingsGoal(
        id = id,
        title = title,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        targetDate = targetDate,
        icon = icon,
        colorHex = colorHex
    )
}
