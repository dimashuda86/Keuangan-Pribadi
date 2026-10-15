package com.example.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CategoryItem
import com.example.domain.model.FamilyMember
import com.example.domain.model.TransactionType
import com.example.domain.model.WalletAccount
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PurpleAccent
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionNumpadBottomSheet(
    amountStr: String,
    titleStr: String,
    transactionType: TransactionType,
    categories: List<CategoryItem>,
    selectedCategory: CategoryItem?,
    members: List<FamilyMember>,
    selectedMember: FamilyMember?,
    wallets: List<WalletAccount>,
    selectedWallet: WalletAccount?,
    destinationWallet: WalletAccount?,
    noteStr: String,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategorySelect: (CategoryItem) -> Unit,
    onMemberSelect: (FamilyMember) -> Unit,
    onWalletSelect: (WalletAccount) -> Unit,
    onDestinationWalletSelect: (WalletAccount) -> Unit,
    onNumpadPress: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showNoteInput by remember { mutableStateOf(noteStr.isNotBlank() || titleStr.isNotBlank()) }

    val formattedDisplay = remember(amountStr) {
        if (amountStr.isEmpty()) "Rp 0"
        else if (amountStr.contains("+") || amountStr.contains("-") || amountStr.contains("×") || amountStr.contains("÷")) {
            amountStr // show calculation formula
        } else {
            val num = amountStr.toDoubleOrNull() ?: 0.0
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.format(num).replace("Rp", "Rp ")
        }
    }

    val typeColor = when (transactionType) {
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.INCOME -> IncomeGreen
        TransactionType.TRANSFER -> PurpleAccent
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("transaction_numpad_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with 3-Way Type Toggle: Pengeluaran, Pemasukan, Transfer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    TypeToggleButton(
                        label = "Pengeluaran",
                        isSelected = transactionType == TransactionType.EXPENSE,
                        activeColor = ExpenseRed,
                        onClick = { onTypeChange(TransactionType.EXPENSE) }
                    )
                    TypeToggleButton(
                        label = "Pemasukan",
                        isSelected = transactionType == TransactionType.INCOME,
                        activeColor = IncomeGreen,
                        onClick = { onTypeChange(TransactionType.INCOME) }
                    )
                    TypeToggleButton(
                        label = "Transfer",
                        isSelected = transactionType == TransactionType.TRANSFER,
                        activeColor = PurpleAccent,
                        onClick = { onTypeChange(TransactionType.TRANSFER) }
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Big Amount Display with Calculator Input
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = typeColor.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (transactionType) {
                            TransactionType.EXPENSE -> "Nominal Pengeluaran"
                            TransactionType.INCOME -> "Nominal Pemasukan"
                            TransactionType.TRANSFER -> "Nominal Transfer Antar Dompet"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDisplay,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (formattedDisplay.length > 14) 22.sp else 28.sp,
                            fontFamily = if (amountStr.contains("+") || amountStr.contains("-")) FontFamily.Monospace else FontFamily.Default
                        ),
                        color = typeColor,
                        maxLines = 1
                    )
                }
            }

            // Wallet/Account Selector (Sumber Dana)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                Text(
                    text = if (transactionType == TransactionType.TRANSFER) "1. Dari Dompet / Rekening:" else "Pilih Sumber Dana / Dompet:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wallets.forEach { wallet ->
                        val isSelected = selectedWallet?.id == wallet.id
                        val walletColor = Color(wallet.colorHex)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) walletColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, walletColor) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onWalletSelect(wallet) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (isSelected) walletColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = wallet.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) walletColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // If Transfer: Destination Wallet Selector
            if (transactionType == TransactionType.TRANSFER) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "2. Ke Dompet Tujuan:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.filter { it.id != selectedWallet?.id }.forEach { wallet ->
                            val isSelected = destinationWallet?.id == wallet.id
                            val walletColor = Color(wallet.colorHex)

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) walletColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, walletColor) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onDestinationWalletSelect(wallet) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = if (isSelected) walletColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) walletColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Categories Selector (only if not transfer)
            if (transactionType != TransactionType.TRANSFER) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "Pilih Kategori",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filteredCats = categories.filter { it.type == transactionType }
                        filteredCats.forEach { category ->
                            val isSelected = selectedCategory?.id == category.id
                            val catColor = Color(category.colorHex)
                            val chipBg by animateColorAsState(
                                targetValue = if (isSelected) catColor else MaterialTheme.colorScheme.surfaceVariant,
                                label = "cat_chip_bg"
                            )
                            val chipTextColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                label = "cat_chip_text"
                            )

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = chipBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onCategorySelect(category) }
                                    .testTag("cat_chip_${category.id}")
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = chipTextColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Member Switcher (Dicatat oleh siapa)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "Dicatat Oleh Anggota",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    members.forEach { member ->
                        val isSelected = selectedMember?.id == member.id
                        val memberColor = Color(member.colorHex)

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) memberColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, memberColor) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onMemberSelect(member) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(memberColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.avatarInitial,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) memberColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Quick Note Toggle
            if (showNoteInput) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    OutlinedTextField(
                        value = titleStr,
                        onValueChange = onTitleChange,
                        label = { Text("Keterangan Singkat (opsional)") },
                        placeholder = { Text("Contoh: Belanja Pasar / Bayar Tagihan") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = noteStr,
                        onValueChange = onNoteChange,
                        label = { Text("Catatan Tambahan") },
                        placeholder = { Text("Catatan struk / keperluan...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { showNoteInput = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Tambah Keterangan / Catatan Struk",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Mini-Calculator & Numpad Grid (0-9, +, -, *, /, =, 000, ⌫)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: 1, 2, 3, +
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumpadKey("1", Modifier.weight(1f)) { onNumpadPress("1") }
                    NumpadKey("2", Modifier.weight(1f)) { onNumpadPress("2") }
                    NumpadKey("3", Modifier.weight(1f)) { onNumpadPress("3") }
                    NumpadKey("+", Modifier.weight(1f), isOperator = true) { onNumpadPress("+") }
                }
                // Row 2: 4, 5, 6, -
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumpadKey("4", Modifier.weight(1f)) { onNumpadPress("4") }
                    NumpadKey("5", Modifier.weight(1f)) { onNumpadPress("5") }
                    NumpadKey("6", Modifier.weight(1f)) { onNumpadPress("6") }
                    NumpadKey("-", Modifier.weight(1f), isOperator = true) { onNumpadPress("-") }
                }
                // Row 3: 7, 8, 9, ×
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumpadKey("7", Modifier.weight(1f)) { onNumpadPress("7") }
                    NumpadKey("8", Modifier.weight(1f)) { onNumpadPress("8") }
                    NumpadKey("9", Modifier.weight(1f)) { onNumpadPress("9") }
                    NumpadKey("×", Modifier.weight(1f), isOperator = true) { onNumpadPress("×") }
                }
                // Row 4: 000, 0, ⌫, =
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumpadKey("000", Modifier.weight(1f), isSpecial = true) { onNumpadPress("000") }
                    NumpadKey("0", Modifier.weight(1f)) { onNumpadPress("0") }
                    NumpadIconKey(
                        icon = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Hapus",
                        modifier = Modifier.weight(1f)
                    ) { onNumpadPress("BACKSPACE") }
                    NumpadKey("=", Modifier.weight(1f), isOperator = true) { onNumpadPress("=") }
                }
            }

            // Bottom Action Bar: Reset, +50rb quick button, and Submit Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onNumpadPress("CLEAR") }
                        .padding(2.dp)
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clickable {
                            val current = amountStr.toLongOrNull() ?: 0L
                            onNumpadPress("CLEAR")
                            val newAmt = current + 50000
                            onNumpadPress(newAmt.toString())
                        }
                        .padding(2.dp)
                ) {
                    Text(
                        text = "+50rb",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                    )
                }

                Button(
                    onClick = onSave,
                    enabled = amountStr.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = typeColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_transaction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (transactionType == TransactionType.TRANSFER) "Proses Transfer" else "Simpan Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeToggleButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) activeColor else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun NumpadKey(
    label: String,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isOperator: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isSpecial -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("numpad_key_$label"),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isSpecial) 15.sp else 18.sp
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun NumpadIconKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("numpad_key_backspace"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
