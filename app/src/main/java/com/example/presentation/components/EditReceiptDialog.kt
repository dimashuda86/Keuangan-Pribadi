package com.example.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.CategoryItem
import com.example.domain.model.FamilyMember
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.WalletAccount
import com.example.ui.theme.GoldSecondary

@Composable
fun EditReceiptDialog(
    transaction: TransactionItem,
    categories: List<CategoryItem>,
    wallets: List<WalletAccount>,
    members: List<FamilyMember>,
    onSaveReceipt: (updatedTransaction: TransactionItem) -> Unit,
    onDismiss: () -> Unit
) {
    var merchant by remember { mutableStateOf(transaction.title) }
    var amountStr by remember { mutableStateOf(transaction.amount.toLong().toString()) }
    var itemsSummary by remember { mutableStateOf(transaction.note) }

    var selectedCat by remember {
        mutableStateOf(categories.find { it.id == transaction.categoryId } ?: categories.firstOrNull { it.type == TransactionType.EXPENSE })
    }
    var selectedWal by remember {
        mutableStateOf(wallets.find { it.id == transaction.walletId } ?: wallets.firstOrNull())
    }
    var selectedMem by remember {
        mutableStateOf(members.find { it.id == transaction.memberId } ?: members.firstOrNull())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("edit_receipt_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GoldSecondary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Menu Edit Struk Belanja",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Koreksi data nota, toko & rincian barang",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toko / Merchant
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Nama Toko / Merchant Struk") },
                    placeholder = { Text("Contoh: Superindo, Indomaret, SPBU") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Total Belanja
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total Nominal Struk (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rincian Barang
                OutlinedTextField(
                    value = itemsSummary,
                    onValueChange = { itemsSummary = it },
                    label = { Text("Rincian Barang / Catatan Belanja") },
                    placeholder = { Text("Contoh: Minyak 2L, Telur 1kg, Roti gandum") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kategori Belanja
                Text("Kategori Belanja:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.filter { it.type == TransactionType.EXPENSE }.forEach { cat ->
                        val isSelected = selectedCat?.id == cat.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(cat.colorHex) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCat = cat }
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dompet Pembayaran
                Text("Dibayar Menggunakan:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    wallets.forEach { wal ->
                        val isSelected = selectedWal?.id == wal.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(wal.colorHex) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedWal = wal }
                        ) {
                            Text(
                                text = wal.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Anggota Keluarga yang Membeli
                Text("Anggota yang Belanja:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { mem ->
                        val isSelected = selectedMem?.id == mem.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMem = mem }
                        ) {
                            Text(
                                text = "${mem.avatarInitial} ${mem.name}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (merchant.isNotBlank() && amount > 0) {
                            onSaveReceipt(
                                transaction.copy(
                                    title = merchant,
                                    amount = amount,
                                    note = itemsSummary,
                                    categoryId = selectedCat?.id ?: transaction.categoryId,
                                    categoryName = selectedCat?.name ?: transaction.categoryName,
                                    walletId = selectedWal?.id ?: transaction.walletId,
                                    walletName = selectedWal?.name ?: transaction.walletName,
                                    memberId = selectedMem?.id ?: transaction.memberId,
                                    memberName = selectedMem?.name ?: transaction.memberName
                                )
                            )
                        }
                    },
                    enabled = merchant.isNotBlank() && amountStr.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Simpan Perubahan Struk", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal")
                }
            }
        }
    }
}
