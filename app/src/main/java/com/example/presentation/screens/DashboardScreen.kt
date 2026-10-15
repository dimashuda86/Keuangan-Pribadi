package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppNavTab
import com.example.domain.model.FamilyMember
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.WalletAccount
import com.example.presentation.components.AddSavingsGoalDialog
import com.example.presentation.components.AddWalletDialog
import com.example.presentation.components.BudgetProgressCard
import com.example.presentation.components.CategoryDetailDialog
import com.example.presentation.components.ConfirmDeleteMemberDialog
import com.example.presentation.components.EditMemberDialog
import com.example.presentation.components.EditReceiptDialog
import com.example.presentation.components.EditSavingsGoalDialog
import com.example.presentation.components.EditWalletDialog
import com.example.presentation.components.ExportReportDialog
import com.example.presentation.components.FamilyFilterDialog
import com.example.presentation.components.FamilyVaultPairingDialog
import com.example.presentation.components.InteractiveDonutChart
import com.example.presentation.components.LoginDialog
import com.example.presentation.components.ReceiptScannerDialog
import com.example.presentation.components.ResetDataDialog
import com.example.presentation.components.SavingsGoalDialog
import com.example.presentation.components.SpeedDialFab
import com.example.presentation.components.TransactionNumpadBottomSheet
import com.example.presentation.viewmodel.ActiveModalType
import com.example.presentation.viewmodel.FamilyFinanceViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PurpleAccent
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FamilyFinanceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val members by viewModel.familyMembers.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val vaultInfo by viewModel.familyVault.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val categorySpending by viewModel.categorySpending.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()

    // Form states
    val amountStr by viewModel.inputAmount.collectAsState()
    val titleStr by viewModel.inputTitle.collectAsState()
    val inputType by viewModel.inputType.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val selectedMem by viewModel.selectedMember.collectAsState()
    val selectedWal by viewModel.selectedWallet.collectAsState()
    val destWal by viewModel.destinationWallet.collectAsState()
    val noteStr by viewModel.inputNote.collectAsState()
    val editingWallet by viewModel.editingWallet.collectAsState()
    val editingSavingsGoal by viewModel.editingSavingsGoal.collectAsState()
    val editingTransaction by viewModel.editingTransaction.collectAsState()
    val editingMember by viewModel.editingMember.collectAsState()
    val memberToDelete by viewModel.memberToDelete.collectAsState()

    val currentMember = members.getOrNull(uiState.activeMemberIndex)

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                SpeedDialFab(
                    expanded = uiState.speedDialExpanded,
                    onToggle = { viewModel.toggleSpeedDial() },
                    onAddIncome = { viewModel.openModal(ActiveModalType.INPUT_TRANSACTION, TransactionType.INCOME) },
                    onAddExpense = { viewModel.openModal(ActiveModalType.INPUT_TRANSACTION, TransactionType.EXPENSE) },
                    onTransfer = { viewModel.openModal(ActiveModalType.TRANSFER, TransactionType.TRANSFER) },
                    onScanReceipt = { viewModel.openModal(ActiveModalType.SCAN_RECEIPT) },
                    onSetBudget = { viewModel.openModal(ActiveModalType.BUDGET_SETTINGS) }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                FamilyBottomNavBar(
                    currentTab = uiState.currentTab,
                    onTabSelected = { viewModel.switchTab(it) }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState.currentTab) {
                    AppNavTab.BERANDA -> {
                        HomeTabContent(
                            viewModel = viewModel,
                            uiState = uiState,
                            transactions = transactions,
                            categories = categories,
                            members = members,
                            wallets = wallets,
                            vaultInfo = vaultInfo,
                            budgets = budgets,
                            categorySpending = categorySpending,
                            monthlySummary = monthlySummary,
                            currentMember = currentMember
                        )
                    }
                    AppNavTab.DOMPET -> {
                        WalletsScreen(viewModel = viewModel)
                    }
                    AppNavTab.ANALISIS -> {
                        AnalyticsScreen(viewModel = viewModel)
                    }
                    AppNavTab.ANGGARAN -> {
                        BudgetsScreen(viewModel = viewModel)
                    }
                    AppNavTab.KELUARGA -> {
                        FamilySettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Full Scrim Overlay when Speed Dial is Expanded
        if (uiState.speedDialExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { viewModel.toggleSpeedDial() }
            )
        }

        // Modal Bottom Sheet: Quick Input with Keypad & Calculator
        if (uiState.activeModal == ActiveModalType.INPUT_TRANSACTION || uiState.activeModal == ActiveModalType.TRANSFER) {
            TransactionNumpadBottomSheet(
                amountStr = amountStr,
                titleStr = titleStr,
                transactionType = inputType,
                categories = categories,
                selectedCategory = selectedCat,
                members = members,
                selectedMember = selectedMem,
                wallets = wallets,
                selectedWallet = selectedWal,
                destinationWallet = destWal,
                noteStr = noteStr,
                onTitleChange = { viewModel.inputTitle.value = it },
                onNoteChange = { viewModel.inputNote.value = it },
                onTypeChange = { viewModel.inputType.value = it },
                onCategorySelect = { viewModel.selectedCategory.value = it },
                onMemberSelect = { viewModel.selectedMember.value = it },
                onWalletSelect = { viewModel.selectedWallet.value = it },
                onDestinationWalletSelect = { viewModel.destinationWallet.value = it },
                onNumpadPress = { viewModel.onNumpadPress(it) },
                onSave = { viewModel.saveTransaction() },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Add New Bank / Wallet Account
        if (uiState.activeModal == ActiveModalType.ADD_WALLET) {
            AddWalletDialog(
                onAddWallet = { name, type, accNum, bal, color ->
                    viewModel.addNewWallet(name, type, accNum, bal, color)
                },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Add Savings Goal
        if (uiState.activeModal == ActiveModalType.ADD_SAVINGS_GOAL) {
            AddSavingsGoalDialog(
                onAddGoal = { title, target, current, targetDate, icon ->
                    viewModel.addNewSavingsGoal(title, target, current, targetDate, icon)
                },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Scan / Receipt OCR
        if (uiState.activeModal == ActiveModalType.SCAN_RECEIPT) {
            ReceiptScannerDialog(
                onReceiptExtracted = { merchant, amount, items ->
                    viewModel.triggerReceiptScanned(merchant, amount, items)
                },
                onShareToBot = { tx, isWa ->
                    viewModel.botShareService.shareTransactionToTelegramOrWhatsApp(tx, isWa)
                },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Savings Goal / Budget Limit
        if (uiState.activeModal == ActiveModalType.BUDGET_SETTINGS) {
            SavingsGoalDialog(
                categories = categories,
                budgets = budgets,
                onSaveBudget = { cat, limit ->
                    viewModel.selectedBudgetCategory.value = cat
                    viewModel.inputBudgetLimit.value = limit.toLong().toString()
                    viewModel.saveBudgetLimit()
                },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Family Vault Pairing & Member Switch
        if (uiState.activeModal == ActiveModalType.VAULT_PAIRING) {
            FamilyVaultPairingDialog(
                vaultInfo = vaultInfo,
                members = members,
                activeMemberIndex = uiState.activeMemberIndex,
                isSyncing = uiState.isSyncingNow,
                onSwitchMember = { viewModel.switchActiveMember(it) },
                onTriggerSync = { viewModel.triggerCloudSync() },
                onTestDailyReminder = { viewModel.triggerDailyReminderDemo() },
                onEditMember = { viewModel.openEditMember(it) },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Floating Detail on Chart Segment Touch
        uiState.selectedCategoryDetail?.let { catDetail ->
            CategoryDetailDialog(
                categorySpending = catDetail,
                allTransactions = transactions,
                onFilterThisCategory = { catId ->
                    viewModel.setFilterCategory(catId)
                },
                onDismiss = { viewModel.selectCategoryDetail(null) }
            )
        }

        // Dialog: Filter Pop-up
        if (uiState.activeModal == ActiveModalType.FILTER_DIALOG) {
            FamilyFilterDialog(
                currentMemberId = uiState.filterMemberId,
                currentCategoryId = uiState.filterCategoryId,
                members = members,
                categories = categories,
                onApplyFilter = { memberId, catId ->
                    viewModel.setFilterMember(memberId)
                    viewModel.setFilterCategory(catId)
                },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Export Report
        if (uiState.activeModal == ActiveModalType.EXPORT_REPORT) {
            ExportReportDialog(
                summary = monthlySummary,
                budgets = budgets,
                transactions = transactions,
                exportService = viewModel.exportService,
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Reset Data (Hapus Semua Data)
        if (uiState.activeModal == ActiveModalType.RESET_DATA) {
            ResetDataDialog(
                onResetCleanSlate = { viewModel.resetAllData(toCleanSlate = true) },
                onResetWalletsOnly = { viewModel.resetWalletsToZero() },
                onResetDemoData = { viewModel.resetAllData(toCleanSlate = false) },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Edit Dompet / E-Wallet
        if (uiState.activeModal == ActiveModalType.EDIT_WALLET) {
            val walletToEdit = editingWallet ?: wallets.firstOrNull()
            if (walletToEdit != null) {
                EditWalletDialog(
                    wallet = walletToEdit,
                    onSaveWallet = { viewModel.updateWallet(it) },
                    onDeleteWallet = { viewModel.deleteWallet(it) },
                    onDismiss = { viewModel.closeModal() }
                )
            }
        }

        // Dialog: Edit Target Tabungan
        if (uiState.activeModal == ActiveModalType.EDIT_SAVINGS_GOAL) {
            val goalToEdit = editingSavingsGoal
            if (goalToEdit != null) {
                EditSavingsGoalDialog(
                    goal = goalToEdit,
                    onSaveGoal = { viewModel.updateSavingsGoal(it) },
                    onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                    onDismiss = { viewModel.closeModal() }
                )
            }
        }

        // Dialog: Edit Struk Belanja
        if (uiState.activeModal == ActiveModalType.EDIT_RECEIPT) {
            val txToEdit = editingTransaction ?: transactions.firstOrNull()
            if (txToEdit != null) {
                EditReceiptDialog(
                    transaction = txToEdit,
                    categories = categories,
                    wallets = wallets,
                    members = members,
                    onSaveReceipt = { viewModel.saveEditedReceipt(it) },
                    onDismiss = { viewModel.closeModal() }
                )
            }
        }

        // Dialog: Login Akun Anggota Keluarga
        if (uiState.activeModal == ActiveModalType.LOGIN) {
            LoginDialog(
                members = members,
                currentMemberIndex = uiState.activeMemberIndex,
                vaultInfo = vaultInfo,
                onLoginSuccess = { selectedIndex ->
                    viewModel.loginMember(selectedIndex)
                },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Edit Nama & Profil Anggota Keluarga
        if (uiState.activeModal == ActiveModalType.EDIT_MEMBER) {
            val memToEdit = editingMember ?: members.getOrNull(uiState.activeMemberIndex)
            if (memToEdit != null) {
                EditMemberDialog(
                    member = memToEdit,
                    onSave = { id, name, role, color ->
                        viewModel.saveEditedMember(id, name, role, color)
                    },
                    onDelete = { member ->
                        viewModel.memberToDelete.value = member
                        viewModel.closeModal()
                    },
                    onDismiss = { viewModel.closeModal() }
                )
            }
        }

        // Dialog: Tambah Anggota Keluarga Baru
        if (uiState.activeModal == ActiveModalType.ADD_MEMBER) {
            EditMemberDialog(
                member = null,
                onSave = { _, name, role, color ->
                    viewModel.addNewMember(name, role, color)
                },
                onDismiss = { viewModel.closeModal() }
            )
        }

        // Dialog: Konfirmasi Hapus Anggota
        if (memberToDelete != null) {
            ConfirmDeleteMemberDialog(
                member = memberToDelete!!,
                onConfirmDelete = {
                    viewModel.deleteMember(memberToDelete!!)
                },
                onDismiss = {
                    viewModel.memberToDelete.value = null
                }
            )
        }
    }
}

@Composable
private fun FamilyBottomNavBar(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarItem(
            selected = currentTab == AppNavTab.BERANDA,
            onClick = { onTabSelected(AppNavTab.BERANDA) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
            label = { Text("Beranda", fontSize = 10.sp, fontWeight = if (currentTab == AppNavTab.BERANDA) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
        )

        NavigationBarItem(
            selected = currentTab == AppNavTab.DOMPET,
            onClick = { onTabSelected(AppNavTab.DOMPET) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Dompet") },
            label = { Text("Dompet", fontSize = 10.sp, fontWeight = if (currentTab == AppNavTab.DOMPET) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
        )

        NavigationBarItem(
            selected = currentTab == AppNavTab.ANALISIS,
            onClick = { onTabSelected(AppNavTab.ANALISIS) },
            icon = { Icon(Icons.Default.PieChart, contentDescription = "Analisis") },
            label = { Text("Analisis", fontSize = 10.sp, fontWeight = if (currentTab == AppNavTab.ANALISIS) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
        )

        NavigationBarItem(
            selected = currentTab == AppNavTab.ANGGARAN,
            onClick = { onTabSelected(AppNavTab.ANGGARAN) },
            icon = { Icon(Icons.Default.Savings, contentDescription = "Anggaran") },
            label = { Text("Anggaran", fontSize = 10.sp, fontWeight = if (currentTab == AppNavTab.ANGGARAN) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
        )

        NavigationBarItem(
            selected = currentTab == AppNavTab.KELUARGA,
            onClick = { onTabSelected(AppNavTab.KELUARGA) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Keluarga") },
            label = { Text("Keluarga", fontSize = 10.sp, fontWeight = if (currentTab == AppNavTab.KELUARGA) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
        )
    }
}

@Composable
private fun HomeTabContent(
    viewModel: FamilyFinanceViewModel,
    uiState: com.example.presentation.viewmodel.FinanceUiState,
    transactions: List<TransactionItem>,
    categories: List<com.example.domain.model.CategoryItem>,
    members: List<FamilyMember>,
    wallets: List<WalletAccount>,
    vaultInfo: com.example.domain.model.FamilyVaultInfo,
    budgets: List<com.example.domain.model.BudgetItem>,
    categorySpending: List<com.example.domain.model.CategorySpending>,
    monthlySummary: com.example.domain.model.MonthlySummary,
    currentMember: FamilyMember?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Family Vault Header with 1-Tap Member Switcher
        item {
            FamilyVaultHeader(
                vaultName = vaultInfo.name,
                pairingCode = vaultInfo.pairingCode,
                currentMember = currentMember,
                syncStatus = uiState.syncStatusMessage,
                onOpenVault = { viewModel.openModal(ActiveModalType.VAULT_PAIRING) },
                onOpenExport = { viewModel.openModal(ActiveModalType.EXPORT_REPORT) },
                onOpenLogin = { viewModel.openModal(ActiveModalType.LOGIN) },
                onOpenReset = { viewModel.openModal(ActiveModalType.RESET_DATA) }
            )
        }

        // Quick Horizontal Member Switcher Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                members.forEachIndexed { index, member ->
                    val isSelected = index == uiState.activeMemberIndex
                    val memberColor = Color(member.colorHex)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) memberColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.switchActiveMember(index) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(if (isSelected) Color.White.copy(alpha = 0.3f) else memberColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(member.avatarInitial, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = member.name.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Balance Summary Card (with Hide/Show Eye Button)
        item {
            BalanceSummaryCard(
                totalIncome = monthlySummary.totalIncome,
                totalExpense = monthlySummary.totalExpense,
                balance = monthlySummary.balance,
                savingsRate = monthlySummary.savingsRate,
                isBalanceVisible = uiState.isBalanceVisible,
                onToggleVisibility = { viewModel.toggleBalanceVisibility() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Quick Action Buttons Row (Direct 1-tap operation)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    label = "+ Pengeluaran",
                    color = ExpenseRed,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openModal(ActiveModalType.INPUT_TRANSACTION, TransactionType.EXPENSE) }
                )
                QuickActionButton(
                    label = "+ Pemasukan",
                    color = IncomeGreen,
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openModal(ActiveModalType.INPUT_TRANSACTION, TransactionType.INCOME) }
                )
                QuickActionButton(
                    label = "Transfer",
                    color = Color(0xFF6366F1),
                    icon = Icons.Default.SwapHoriz,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openModal(ActiveModalType.TRANSFER, TransactionType.TRANSFER) }
                )
                QuickActionButton(
                    label = "Scan",
                    color = GoldSecondary,
                    icon = Icons.Default.CameraAlt,
                    modifier = Modifier.weight(0.9f),
                    onClick = { viewModel.openModal(ActiveModalType.SCAN_RECEIPT) }
                )
            }
        }

        // Mini Horizontal Wallets Overview
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Akun Finansial Keluarga",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.switchTab(AppNavTab.DOMPET) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    wallets.forEach { wal ->
                        MiniWalletCard(wallet = wal, isVisible = uiState.isBalanceVisible)
                    }
                }
            }
        }

        // Search & Filter Toolbar
        item {
            FilterToolbar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                filterMemberId = uiState.filterMemberId,
                filterCategoryId = uiState.filterCategoryId,
                members = members,
                categories = categories,
                onOpenFilter = { viewModel.openModal(ActiveModalType.FILTER_DIALOG) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Interactive Donut Chart (Touch Segment with pop-up detail)
        item {
            InteractiveDonutChart(
                spendingList = categorySpending,
                totalExpense = monthlySummary.totalExpense,
                onCategoryClick = { viewModel.selectCategoryDetail(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Category Budgets Progress Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kontrol Anggaran Bulanan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable {
                        viewModel.openModal(ActiveModalType.BUDGET_SETTINGS)
                    }
                ) {
                    Text(
                        text = "+ Atur Limit",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        items(budgets.take(3)) { budget ->
            BudgetProgressCard(
                budget = budget,
                onEditClick = { viewModel.openModal(ActiveModalType.BUDGET_SETTINGS) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Transaksi Terbaru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${transactions.size} Tercatat",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Transactions List
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum Ada Transaksi",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gunakan tombol cepat di atas untuk mencatat belanja pertama",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    onEditReceipt = { viewModel.openEditReceipt(tx) },
                    onShare = { isWa ->
                        viewModel.botShareService.shareTransactionToTelegramOrWhatsApp(tx, isWa)
                    },
                    onDelete = { viewModel.deleteTransaction(tx.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MiniWalletCard(
    wallet: WalletAccount,
    isVisible: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(wallet.colorHex).copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(wallet.colorHex).copy(alpha = 0.3f)),
        modifier = Modifier.width(140.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = wallet.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isVisible) formatRupiah(wallet.balance) else "Rp •••••",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(wallet.colorHex),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FamilyVaultHeader(
    vaultName: String,
    pairingCode: String,
    currentMember: FamilyMember?,
    syncStatus: String,
    onOpenVault: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenReset: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenVault)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            currentMember?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentMember?.avatarInitial ?: "👨‍👩‍👧‍👦",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentMember?.name ?: "Keluarga Cemara",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Kode Vault",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = IncomeGreen
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$syncStatus • $pairingCode",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Login Menu Button
                IconButton(
                    onClick = onOpenLogin,
                    modifier = Modifier.size(36.dp).testTag("login_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Menu Login Akun",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Reset Data Menu Button
                IconButton(
                    onClick = onOpenReset,
                    modifier = Modifier.size(36.dp).testTag("reset_data_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Menu Reset Data",
                        tint = ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Export Button
                IconButton(
                    onClick = onOpenExport,
                    modifier = Modifier.size(36.dp).testTag("export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Ekspor Laporan Bulanan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceSummaryCard(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    savingsRate: Int,
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balance_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Aset / Saldo Brankas",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(28.dp).testTag("toggle_balance_button")
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isBalanceVisible) "Sembunyikan Saldo" else "Tampilkan Saldo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isBalanceVisible) formatRupiah(balance) else "Rp ••••••••••",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = IncomeGreen.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(IncomeGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Pemasukan",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isBalanceVisible) formatRupiah(totalIncome) else "Rp •••••",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = IncomeGreen,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ExpenseRed.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ExpenseRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Pengeluaran",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isBalanceVisible) formatRupiah(totalExpense) else "Rp •••••",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExpenseRed,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterToolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterMemberId: String,
    filterCategoryId: String,
    members: List<FamilyMember>,
    categories: List<com.example.domain.model.CategoryItem>,
    onOpenFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMemberName = remember(filterMemberId, members) {
        if (filterMemberId == "ALL") "Semua Anggota" else members.find { it.id == filterMemberId }?.name ?: "Filter"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari belanja, nota, akun...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (filterMemberId != "ALL" || filterCategoryId != "ALL") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenFilter)
                .testTag("open_filter_dialog_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier.size(18.dp),
                    tint = if (filterMemberId != "ALL" || filterCategoryId != "ALL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = activeMemberName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (filterMemberId != "ALL" || filterCategoryId != "ALL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TransactionRowItem(
    transaction: TransactionItem,
    onEditReceipt: () -> Unit,
    onShare: (isWhatsApp: Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val isTransfer = transaction.type == TransactionType.TRANSFER
    val amountColor = when {
        isTransfer -> PurpleAccent
        isExpense -> ExpenseRed
        else -> IncomeGreen
    }
    val sign = when {
        isTransfer -> "⇄ "
        isExpense -> "- "
        else -> "+ "
    }
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEditReceipt)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when {
                                isTransfer -> Color(0xFF6366F1)
                                transaction.memberName.contains("Ayah") -> Color(0xFF2563EB)
                                transaction.memberName.contains("Ibu") -> Color(0xFFDB2777)
                                transaction.memberName.contains("Kakak") -> Color(0xFF059669)
                                else -> Color(0xFFEA580C)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTransfer) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = transaction.memberName.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isTransfer) {
                            "${transaction.walletName} ➔ ${transaction.destinationWalletName ?: "Dompet"} • ${dateFormat.format(Date(transaction.timestamp))}"
                        } else {
                            "${transaction.categoryName} • ${transaction.walletName} • ${transaction.memberName}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    if (transaction.note.isNotBlank()) {
                        Text(
                            text = "Nota: ${transaction.note}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = sign + formatRupiah(transaction.amount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditReceipt,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Struk & Transaksi",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onShare(true) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Forward ke WhatsApp/Telegram Bot",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Transaksi",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}
