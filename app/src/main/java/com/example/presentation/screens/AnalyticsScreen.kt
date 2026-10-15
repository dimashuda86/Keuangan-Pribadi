package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TransactionType
import com.example.presentation.components.InteractiveDonutChart
import com.example.presentation.viewmodel.FamilyFinanceViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: FamilyFinanceViewModel,
    modifier: Modifier = Modifier
) {
    val categorySpending by viewModel.categorySpending.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val members by viewModel.familyMembers.collectAsState()

    val totalExpense = monthlySummary.totalExpense

    // Calculate spending per family member
    val spendingPerMember = members.map { member ->
        val memberSpent = transactions
            .filter { it.type == TransactionType.EXPENSE && it.memberId == member.id }
            .sumOf { it.amount }
        val pct = if (totalExpense > 0) (memberSpent / totalExpense).toFloat() else 0f
        Triple(member, memberSpent, pct)
    }.sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "Analisis Keuangan Keluarga",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Laporan komparasi dan kebiasaan belanja keluarga",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Interactive Donut Chart
        item {
            InteractiveDonutChart(
                spendingList = categorySpending,
                totalExpense = totalExpense,
                onCategoryClick = { viewModel.selectCategoryDetail(it) },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Family Members Spending Distribution
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Kontribusi Pengeluaran Per Anggota",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    spendingPerMember.forEach { (member, spent, pct) ->
                        val memberColor = Color(member.colorHex)
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier.size(24.dp).background(memberColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(member.avatarInitial, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Text(member.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                }
                                Text("${(pct * 100).toInt()}% • ${formatRupiah(spent)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = memberColor)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { pct.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = memberColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        // Category breakdown list
        item {
            Text(
                text = "Peringkat Belanja Kategori",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(categorySpending) { spending ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = CircleShape, color = Color(spending.colorHex), modifier = Modifier.size(10.dp)) {}
                        Column {
                            Text(spending.categoryName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("${spending.transactionCount} Transaksi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatRupiah(spending.totalAmount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(spending.colorHex))
                        Text("${(spending.percentage * 100).toInt()}% dari total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
