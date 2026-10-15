package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.FamilyFinanceRepository
import com.example.domain.model.AppNavTab
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
import com.example.service.BotShareService
import com.example.service.ExportService
import com.example.service.SmartNotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ActiveModalType {
    NONE,
    INPUT_TRANSACTION,
    TRANSFER,
    SCAN_RECEIPT,
    BUDGET_SETTINGS,
    ADD_WALLET,
    EDIT_WALLET,
    ADD_SAVINGS_GOAL,
    EDIT_SAVINGS_GOAL,
    EDIT_RECEIPT,
    EDIT_MEMBER,
    ADD_MEMBER,
    VAULT_PAIRING,
    EXPORT_REPORT,
    FILTER_DIALOG,
    RESET_DATA,
    LOGIN
}

data class FinanceUiState(
    val currentTab: AppNavTab = AppNavTab.BERANDA,
    val isBalanceVisible: Boolean = true,
    val speedDialExpanded: Boolean = false,
    val activeModal: ActiveModalType = ActiveModalType.NONE,
    val selectedCategoryDetail: CategorySpending? = null,
    val filterMemberId: String = "ALL",
    val filterCategoryId: String = "ALL",
    val filterWalletId: String = "ALL",
    val searchQuery: String = "",
    val activeMemberIndex: Int = 0,
    val isSyncingNow: Boolean = false,
    val syncStatusMessage: String = "Tersinkronisasi Realtime"
)

class FamilyFinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = FamilyFinanceRepository(database)
    val notificationManager = SmartNotificationManager(application)
    val botShareService = BotShareService(application)
    val exportService = ExportService(application)

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    val wallets: StateFlow<List<WalletAccount>> = repository.wallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryItem>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyMembers: StateFlow<List<FamilyMember>> = repository.familyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyVault: StateFlow<FamilyVaultInfo> = repository.familyVault
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyVaultInfo())

    val budgets: StateFlow<List<BudgetItem>> = repository.budgetsWithSpending
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categorySpending: StateFlow<List<CategorySpending>> = repository.categorySpendingBreakdown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummary: StateFlow<MonthlySummary> = repository.monthlySummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary(0.0, 0.0, 0.0, 0))

    // Filtered transactions stream
    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        repository.transactions,
        _uiState
    ) { allTransactions, state ->
        allTransactions.filter { tx ->
            val matchesMember = state.filterMemberId == "ALL" || tx.memberId == state.filterMemberId
            val matchesCategory = state.filterCategoryId == "ALL" || tx.categoryId == state.filterCategoryId
            val matchesWallet = state.filterWalletId == "ALL" || tx.walletId == state.filterWalletId || tx.destinationWalletId == state.filterWalletId
            val matchesSearch = state.searchQuery.isBlank() ||
                    tx.title.contains(state.searchQuery, ignoreCase = true) ||
                    tx.categoryName.contains(state.searchQuery, ignoreCase = true) ||
                    tx.walletName.contains(state.searchQuery, ignoreCase = true) ||
                    tx.memberName.contains(state.searchQuery, ignoreCase = true)
            matchesMember && matchesCategory && matchesWallet && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transaction Form States
    val inputAmount = MutableStateFlow("")
    val inputTitle = MutableStateFlow("")
    val inputType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedCategory = MutableStateFlow<CategoryItem?>(null)
    val selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedWallet = MutableStateFlow<WalletAccount?>(null)
    val destinationWallet = MutableStateFlow<WalletAccount?>(null)
    val inputNote = MutableStateFlow("")
    val receiptUri = MutableStateFlow<String?>(null)

    // Budget Editor States
    val selectedBudgetCategory = MutableStateFlow<CategoryItem?>(null)
    val inputBudgetLimit = MutableStateFlow("")

    // Edit Modals State
    val editingWallet = MutableStateFlow<WalletAccount?>(null)
    val editingSavingsGoal = MutableStateFlow<SavingsGoal?>(null)
    val editingTransaction = MutableStateFlow<TransactionItem?>(null)
    val editingMember = MutableStateFlow<FamilyMember?>(null)
    val memberToDelete = MutableStateFlow<FamilyMember?>(null)

    fun switchTab(tab: AppNavTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab, speedDialExpanded = false)
    }

    fun toggleBalanceVisibility() {
        _uiState.value = _uiState.value.copy(isBalanceVisible = !_uiState.value.isBalanceVisible)
    }

    fun toggleSpeedDial() {
        _uiState.value = _uiState.value.copy(speedDialExpanded = !_uiState.value.speedDialExpanded)
    }

    fun openModal(type: ActiveModalType, defaultType: TransactionType = TransactionType.EXPENSE) {
        _uiState.value = _uiState.value.copy(
            speedDialExpanded = false,
            activeModal = type
        )
        if (type == ActiveModalType.INPUT_TRANSACTION || type == ActiveModalType.TRANSFER) {
            inputType.value = if (type == ActiveModalType.TRANSFER) TransactionType.TRANSFER else defaultType
            inputAmount.value = ""
            inputTitle.value = if (type == ActiveModalType.TRANSFER) "Transfer Antar Dompet" else ""
            inputNote.value = ""
            receiptUri.value = null

            // Defaults
            val available = categories.value.filter { it.type == defaultType }
            selectedCategory.value = available.firstOrNull() ?: categories.value.firstOrNull()

            val memberList = familyMembers.value
            if (memberList.isNotEmpty()) {
                val idx = _uiState.value.activeMemberIndex.coerceIn(0, memberList.size - 1)
                selectedMember.value = memberList[idx]
            }

            val walletList = wallets.value
            if (walletList.isNotEmpty()) {
                selectedWallet.value = walletList.firstOrNull()
                destinationWallet.value = walletList.getOrNull(1) ?: walletList.firstOrNull()
            }
        }
    }

    fun closeModal() {
        _uiState.value = _uiState.value.copy(activeModal = ActiveModalType.NONE)
    }

    fun selectCategoryDetail(categorySpending: CategorySpending?) {
        _uiState.value = _uiState.value.copy(selectedCategoryDetail = categorySpending)
    }

    fun setFilterMember(memberId: String) {
        _uiState.value = _uiState.value.copy(filterMemberId = memberId)
    }

    fun setFilterCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(filterCategoryId = categoryId)
    }

    fun setFilterWallet(walletId: String) {
        _uiState.value = _uiState.value.copy(filterWalletId = walletId)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun switchActiveMember(index: Int) {
        val members = familyMembers.value
        if (index in members.indices) {
            _uiState.value = _uiState.value.copy(activeMemberIndex = index)
            selectedMember.value = members[index]
        }
    }

    // Mini-calculator & numpad handler: supports 0-9, 000, +, -, *, /, =, C, ⌫
    fun onNumpadPress(key: String) {
        val current = inputAmount.value
        when (key) {
            "CLEAR" -> inputAmount.value = ""
            "BACKSPACE" -> {
                if (current.isNotEmpty()) {
                    inputAmount.value = current.dropLast(1)
                }
            }
            "000" -> {
                if (current.isNotEmpty() && !current.endsWith("+") && !current.endsWith("-") && !current.endsWith("×")) {
                    inputAmount.value = current + "000"
                }
            }
            "+", "-", "×", "÷" -> {
                // If expression ends with operator, replace it
                if (current.isNotEmpty()) {
                    val last = current.last()
                    if (last in listOf('+', '-', '×', '÷')) {
                        inputAmount.value = current.dropLast(1) + key
                    } else {
                        inputAmount.value = current + key
                    }
                }
            }
            "=" -> {
                evaluateExpression()
            }
            else -> {
                // Digits 0-9
                inputAmount.value = current + key
            }
        }
    }

    fun evaluateExpression() {
        val expr = inputAmount.value
        if (expr.isBlank()) return
        try {
            // Simple 2-operand or chained evaluator
            val cleaned = expr.replace("×", "*").replace("÷", "/")
            val tokens = Regex("(?<=[-+*/])|(?=[-+*/])").split(cleaned).map { it.trim() }.filter { it.isNotEmpty() }
            if (tokens.isEmpty()) return

            var result = tokens[0].toDoubleOrNull() ?: 0.0
            var i = 1
            while (i < tokens.size) {
                val op = tokens[i]
                val nextVal = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                when (op) {
                    "+" -> result += nextVal
                    "-" -> result -= nextVal
                    "*" -> result *= nextVal
                    "/" -> if (nextVal != 0.0) result /= nextVal
                }
                i += 2
            }
            inputAmount.value = result.toLong().toString()
        } catch (_: Exception) {
            // keep as is
        }
    }

    fun saveTransaction() {
        // If there's an active formula, evaluate it first
        evaluateExpression()

        val amount = inputAmount.value.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) return

        val member = selectedMember.value ?: familyMembers.value.firstOrNull() ?: return
        val category = selectedCategory.value ?: categories.value.firstOrNull() ?: return
        val wallet = selectedWallet.value ?: wallets.value.firstOrNull()
        val destWallet = destinationWallet.value

        val defaultTitle = when (inputType.value) {
            TransactionType.EXPENSE -> category.name
            TransactionType.INCOME -> "Pemasukan ${category.name}"
            TransactionType.TRANSFER -> "Transfer ke ${destWallet?.name ?: "Dompet"}"
        }
        val title = inputTitle.value.ifBlank { defaultTitle }

        viewModelScope.launch {
            val transaction = TransactionItem(
                title = title,
                amount = amount,
                type = inputType.value,
                categoryId = if (inputType.value == TransactionType.TRANSFER) "cat_transfer" else category.id,
                categoryName = if (inputType.value == TransactionType.TRANSFER) "Transfer Antar Dompet" else category.name,
                memberId = member.id,
                memberName = member.name,
                memberRole = member.role,
                walletId = wallet?.id ?: "acc_cash",
                walletName = wallet?.name ?: "Dompet Tunai",
                destinationWalletId = if (inputType.value == TransactionType.TRANSFER) destWallet?.id else null,
                destinationWalletName = if (inputType.value == TransactionType.TRANSFER) destWallet?.name else null,
                note = inputNote.value,
                receiptUri = receiptUri.value,
                timestamp = System.currentTimeMillis()
            )

            repository.addTransaction(transaction)

            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Tersinkronisasi Otomatis (${member.name})"
            )

            // Notifications
            notificationManager.showTransactionLoggedAlert(
                memberName = member.name,
                title = title,
                amount = amount,
                isExpense = inputType.value == TransactionType.EXPENSE
            )

            // Over-budget alerts
            if (inputType.value == TransactionType.EXPENSE) {
                val matchedBudget = budgets.value.find { it.categoryId == category.id }
                if (matchedBudget != null) {
                    val newSpent = matchedBudget.currentSpent + amount
                    val newPct = if (matchedBudget.monthlyLimit > 0) ((newSpent / matchedBudget.monthlyLimit) * 100).toInt() else 0
                    if (newPct >= matchedBudget.alertThresholdPercent) {
                        notificationManager.showOverBudgetAlert(
                            categoryName = matchedBudget.categoryName,
                            percentage = newPct,
                            limit = matchedBudget.monthlyLimit,
                            spent = newSpent
                        )
                    }
                }
            }

            closeModal()
        }
    }

    fun addNewWallet(name: String, type: String, accNumber: String, balance: Double, colorHex: Long) {
        val member = selectedMember.value ?: familyMembers.value.firstOrNull() ?: return
        viewModelScope.launch {
            val newWallet = WalletAccount(
                id = "acc_${System.currentTimeMillis()}",
                name = name,
                type = type,
                accountNumber = accNumber,
                balance = balance,
                colorHex = colorHex,
                holderMemberId = member.id,
                holderName = member.name
            )
            repository.addWallet(newWallet)
            closeModal()
        }
    }

    fun addNewSavingsGoal(title: String, targetAmount: Double, currentAmount: Double, targetDate: String, icon: String) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                id = "g_${System.currentTimeMillis()}",
                title = title,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate,
                icon = icon
            )
            repository.addSavingsGoal(goal)
            closeModal()
        }
    }

    fun saveBudgetLimit() {
        val cat = selectedBudgetCategory.value ?: return
        val limit = inputBudgetLimit.value.toDoubleOrNull() ?: return
        if (limit <= 0) return

        viewModelScope.launch {
            val budget = BudgetItem(
                id = "b_${cat.id}",
                categoryId = cat.id,
                categoryName = cat.name,
                monthlyLimit = limit,
                alertThresholdPercent = 80
            )
            repository.saveBudget(budget)
            closeModal()
        }
    }

    fun openEditWallet(wallet: WalletAccount) {
        editingWallet.value = wallet
        openModal(ActiveModalType.EDIT_WALLET)
    }

    fun updateWallet(wallet: WalletAccount) {
        viewModelScope.launch {
            repository.updateWallet(wallet)
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Dompet/E-Wallet '${wallet.name}' Berhasil Diperbarui"
            )
            closeModal()
        }
    }

    fun deleteWallet(id: String) {
        viewModelScope.launch {
            repository.deleteWallet(id)
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Akun Dompet Dihapus"
            )
            closeModal()
        }
    }

    fun openEditSavingsGoal(goal: SavingsGoal) {
        editingSavingsGoal.value = goal
        openModal(ActiveModalType.EDIT_SAVINGS_GOAL)
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.updateSavingsGoal(goal)
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Target Tabungan '${goal.title}' Diperbarui"
            )
            closeModal()
        }
    }

    fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Target Tabungan Dihapus"
            )
            closeModal()
        }
    }

    fun openEditReceipt(transaction: TransactionItem) {
        editingTransaction.value = transaction
        openModal(ActiveModalType.EDIT_RECEIPT)
    }

    fun saveEditedReceipt(transaction: TransactionItem) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Struk '${transaction.title}' Berhasil Disimpan"
            )
            closeModal()
        }
    }

    fun openEditMember(member: FamilyMember) {
        editingMember.value = member
        openModal(ActiveModalType.EDIT_MEMBER)
    }

    fun openAddMember() {
        openModal(ActiveModalType.ADD_MEMBER)
    }

    fun saveEditedMember(id: String, name: String, role: String, colorHex: Long) {
        val trimmedName = name.trim()
        val trimmedRole = role.trim().ifEmpty { "Anggota Keluarga" }
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val updated = FamilyMember(
                id = id,
                name = trimmedName,
                role = trimmedRole,
                colorHex = colorHex
            )
            repository.updateFamilyMember(updated)
            if (selectedMember.value?.id == id) {
                selectedMember.value = updated
            }
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Nama Anggota '$trimmedName' Berhasil Disimpan"
            )
            notificationManager.showSyncBroadcastAlert(
                "Profil Anggota Diperbarui",
                "Perubahan profil '$trimmedName' ($trimmedRole) telah disinkronkan ke seluruh HP anggota keluarga."
            )
            closeModal()
        }
    }

    fun addNewMember(name: String, role: String, colorHex: Long) {
        val trimmedName = name.trim()
        val trimmedRole = role.trim().ifEmpty { "Anggota Keluarga" }
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val newMember = FamilyMember(
                id = "mem_${System.currentTimeMillis()}",
                name = trimmedName,
                role = trimmedRole,
                colorHex = colorHex
            )
            repository.addFamilyMember(newMember)
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Anggota Baru '$trimmedName' Berhasil Ditambahkan"
            )
            notificationManager.showSyncBroadcastAlert(
                "Anggota Keluarga Baru",
                "Anggota '$trimmedName' berhasil ditambahkan ke Brankas Finansial."
            )
            closeModal()
        }
    }

    fun deleteMember(member: FamilyMember) {
        val currentList = familyMembers.value
        if (currentList.size <= 1) {
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Gagal: Minimal harus ada 1 anggota keluarga!"
            )
            notificationManager.showSyncBroadcastAlert(
                "Tidak Dapat Menghapus",
                "Minimal harus tersisa 1 anggota keluarga dalam brankas finansial."
            )
            memberToDelete.value = null
            return
        }

        viewModelScope.launch {
            repository.deleteFamilyMember(member.id)
            val remaining = currentList.filter { it.id != member.id }
            val currentIdx = _uiState.value.activeMemberIndex
            val newIdx = if (currentIdx >= remaining.size) 0 else currentIdx
            _uiState.value = _uiState.value.copy(
                activeMemberIndex = newIdx,
                filterMemberId = if (_uiState.value.filterMemberId == member.id) "ALL" else _uiState.value.filterMemberId,
                syncStatusMessage = "Anggota '${member.name}' Berhasil Dihapus"
            )
            if (selectedMember.value?.id == member.id) {
                selectedMember.value = remaining.getOrNull(newIdx)
            }
            notificationManager.showSyncBroadcastAlert(
                "Anggota Dihapus",
                "Profil '${member.name}' telah dihapus dari Brankas Finansial."
            )
            memberToDelete.value = null
            closeModal()
        }
    }

    fun resetWalletsToZero() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncingNow = true,
                syncStatusMessage = "Mereset seluruh saldo dompet ke Rp 0..."
            )
            repository.resetAllWalletsToZero()
            delay(600)
            _uiState.value = _uiState.value.copy(
                isSyncingNow = false,
                syncStatusMessage = "Saldo Dompet & E-Wallet Telah Dikosongkan"
            )
            notificationManager.showSyncBroadcastAlert(
                "Reset Saldo Dompet Selesai",
                "Semua saldo dompet dan e-wallet telah diatur ke Rp 0 secara otomatis."
            )
            closeModal()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingNow = true, syncStatusMessage = "Menyinkronkan ke Cloud...")
            delay(1000)
            _uiState.value = _uiState.value.copy(
                isSyncingNow = false,
                syncStatusMessage = "Tersinkronisasi Realtime"
            )
        }
    }

    fun triggerReceiptScanned(merchant: String, amount: Double, itemsSummary: String) {
        inputAmount.value = amount.toLong().toString()
        inputTitle.value = merchant
        inputNote.value = itemsSummary
        inputType.value = TransactionType.EXPENSE
        val lower = merchant.lowercase()
        val cat = categories.value.find { c ->
            when {
                lower.contains("mart") || lower.contains("pasar") || lower.contains("supermarket") -> c.id == "cat_dapur"
                lower.contains("resto") || lower.contains("warung") || lower.contains("kopi") -> c.id == "cat_makan"
                lower.contains("spbu") || lower.contains("pertamina") || lower.contains("shell") -> c.id == "cat_transport"
                else -> c.id == "cat_dapur"
            }
        } ?: categories.value.firstOrNull { it.type == TransactionType.EXPENSE }
        selectedCategory.value = cat
        openModal(ActiveModalType.INPUT_TRANSACTION, TransactionType.EXPENSE)
    }

    fun triggerDailyReminderDemo() {
        notificationManager.showDailyReminder()
    }

    fun resetAllData(toCleanSlate: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncingNow = true,
                syncStatusMessage = "Mereset data dan menyinkronkan ke HP keluarga..."
            )
            repository.resetData(toCleanSlate)
            delay(800)
            _uiState.value = _uiState.value.copy(
                isSyncingNow = false,
                syncStatusMessage = "Tersinkronisasi Otomatis"
            )
            val resetMsg = if (toCleanSlate) {
                "Semua transaksi telah dihapus bersih (saldo Rp 0) dan disinkronkan otomatis ke HP anggota keluarga."
            } else {
                "Data demo awal telah dimuat ulang dan disinkronkan otomatis ke HP anggota keluarga."
            }
            notificationManager.showSyncBroadcastAlert("Reset Data Berhasil", resetMsg)
            closeModal()
        }
    }

    fun loginMember(memberIndex: Int) {
        switchActiveMember(memberIndex)
        viewModelScope.launch {
            val member = familyMembers.value.getOrNull(memberIndex)
            val name = member?.name ?: "Anggota"
            _uiState.value = _uiState.value.copy(
                syncStatusMessage = "Tersambung sebagai $name"
            )
            notificationManager.showSyncBroadcastAlert(
                "Login Akun Berhasil",
                "Selamat datang kembali, $name! Terkoneksi otomatis ke Brankas Keluarga."
            )
            closeModal()
        }
    }
}
