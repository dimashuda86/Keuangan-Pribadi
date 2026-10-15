package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PurpleAccent

@Composable
fun SpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    onScanReceipt: () -> Unit,
    onSetBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "fab_rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Speed Dial Items
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                SpeedDialItem(
                    label = "Atur Limit Anggaran",
                    icon = Icons.Default.Flag,
                    iconColor = Color.White,
                    backgroundColor = PurpleAccent,
                    onClick = onSetBudget,
                    testTag = "speed_dial_budget"
                )

                SpeedDialItem(
                    label = "Scan / Foto Struk",
                    icon = Icons.Default.CameraAlt,
                    iconColor = Color.White,
                    backgroundColor = GoldSecondary,
                    onClick = onScanReceipt,
                    testTag = "speed_dial_scan"
                )

                SpeedDialItem(
                    label = "Transfer Antar Dompet",
                    icon = Icons.Default.SwapHoriz,
                    iconColor = Color.White,
                    backgroundColor = Color(0xFF6366F1),
                    onClick = onTransfer,
                    testTag = "speed_dial_transfer"
                )

                SpeedDialItem(
                    label = "Catat Pengeluaran",
                    icon = Icons.Default.Remove,
                    iconColor = Color.White,
                    backgroundColor = ExpenseRed,
                    onClick = onAddExpense,
                    testTag = "speed_dial_expense"
                )

                SpeedDialItem(
                    label = "Tambah Pemasukan",
                    icon = Icons.Default.Add,
                    iconColor = Color.White,
                    backgroundColor = IncomeGreen,
                    onClick = onAddIncome,
                    testTag = "speed_dial_income"
                )
            }
        }

        // Main Rotating FAB (+)
        FloatingActionButton(
            onClick = onToggle,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .size(56.dp)
                .testTag("main_speed_dial_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Tutup Menu" else "Buka Menu Tambah",
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(end = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
