package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.FamilyMember
import com.example.ui.theme.ExpenseRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditMemberDialog(
    member: FamilyMember? = null,
    onSave: (id: String, name: String, role: String, colorHex: Long) -> Unit,
    onDelete: ((FamilyMember) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isEditMode = member != null

    var name by remember { mutableStateOf(member?.name ?: "") }
    var role by remember { mutableStateOf(member?.role ?: "Anggota Keluarga") }
    var selectedColor by remember { mutableStateOf(member?.colorHex ?: 0xFF2563EB) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val roleSuggestions = listOf(
        "Kepala Keluarga",
        "Bendahara Keluarga",
        "Ibu Rumah Tangga",
        "Anak Pertama",
        "Anak Kedua",
        "Anak Ketiga",
        "Kakek / Nenek",
        "Anggota Keluarga"
    )

    val colorPresets = listOf(
        0xFF2563EB, // Royal Blue
        0xFFDB2777, // Deep Rose
        0xFF059669, // Jade Green
        0xFFEA580C, // Coral Orange
        0xFF7C3AED, // Deep Purple
        0xFFD97706, // Amber
        0xFF0284C7, // Sky Blue
        0xFFDC2626  // Crimson Red
    )

    val previewInitial = name.trim().firstOrNull()?.toString()?.uppercase() ?: "?"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("edit_member_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog Header
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
                            color = Color(selectedColor).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Person else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color(selectedColor),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isEditMode) "Edit Nama Anggota" else "Tambah Anggota Baru",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEditMode) "Perbarui nama & peran anggota" else "Tambahkan profil anggota keluarga",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_member_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Live Preview
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(selectedColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = previewInitial,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Nama Anggota
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) errorMessage = null
                    },
                    label = { Text("Nama Lengkap / Panggilan Anggota *") },
                    placeholder = { Text("Contoh: Ayah (Budi) atau Budi") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Nama yang tampil pada transaksi dan laporan brankas")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input Peran Anggota
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Peran dalam Keluarga") },
                    placeholder = { Text("Contoh: Kepala Keluarga / Ibu / Kakak") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_role_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Quick Suggestion Chips
                Text(
                    text = "Pilih Cepat Peran:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roleSuggestions.forEach { suggestion ->
                        val isSelected = role.equals(suggestion, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { role = suggestion }
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Selection Presets
                Text(
                    text = "Warna Identitas Profil:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPresets.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        if (trimmedName.isBlank()) {
                            errorMessage = "Nama anggota tidak boleh kosong!"
                            return@Button
                        }
                        val trimmedRole = role.trim().ifEmpty { "Anggota Keluarga" }
                        val id = member?.id ?: "mem_${System.currentTimeMillis()}"
                        onSave(id, trimmedName, trimmedRole, selectedColor)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_member_button")
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Save else Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Simpan Perubahan Nama" else "Tambah Anggota",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delete Button (if in Edit Mode and onDelete provided)
                if (isEditMode && onDelete != null && member != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            onDelete(member)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ExpenseRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_member_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hapus Anggota Ini",
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
