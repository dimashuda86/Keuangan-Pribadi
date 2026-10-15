package com.example.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CategorySpending
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.atan2

@Composable
fun InteractiveDonutChart(
    spendingList: List<CategorySpending>,
    totalExpense: Double,
    onCategoryClick: (CategorySpending) -> Unit,
    modifier: Modifier = Modifier
) {
    if (spendingList.isEmpty() || totalExpense <= 0) {
        EmptyChartCard(modifier)
        return
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(spendingList) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
        if (selectedIndex !in spendingList.indices) {
            selectedIndex = 0
        }
    }

    val activeCategory = spendingList.getOrNull(selectedIndex) ?: spendingList.first()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_donut_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distribusi Pengeluaran Keluarga",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sentuh Segmen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart Canvas with Center Text
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                val chartAngles = remember(spendingList) {
                    var currentAngle = -90f
                    spendingList.map { spending ->
                        val sweep = spending.percentage * 360f
                        val start = currentAngle
                        currentAngle += sweep
                        Pair(start, sweep)
                    }
                }

                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .pointerInput(spendingList) {
                            detectTapGestures { tapOffset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = tapOffset.x - center.x
                                val dy = tapOffset.y - center.y
                                val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                                val minRadius = size.width * 0.22f
                                val maxRadius = size.width * 0.52f

                                if (distance in minRadius..maxRadius) {
                                    // Calculate angle in degrees (0..360, 0 is right, 90 down)
                                    var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    // Shift so -90 deg (top) is 0
                                    var normalized = deg + 90f
                                    if (normalized < 0) normalized += 360f

                                    var accumulated = 0f
                                    for ((idx, spending) in spendingList.withIndex()) {
                                        val sweep = spending.percentage * 360f
                                        if (normalized in accumulated..(accumulated + sweep)) {
                                            selectedIndex = idx
                                            break
                                        }
                                        accumulated += sweep
                                    }
                                }
                            }
                        }
                ) {
                    val strokeWidthBase = 28.dp.toPx()
                    val strokeWidthSelected = 38.dp.toPx()
                    val arcSize = size.width - strokeWidthSelected

                    var startAngle = -90f
                    spendingList.forEachIndexed { index, spending ->
                        val isSelected = index == selectedIndex
                        val sweepAngle = (spending.percentage * 360f) * animatedProgress.value
                        val stroke = if (isSelected) strokeWidthSelected else strokeWidthBase
                        val color = Color(spending.colorHex)

                        drawArc(
                            color = if (isSelected) color else color.copy(alpha = 0.85f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2.5f, // slight gap between segments
                            useCenter = false,
                            topLeft = Offset((size.width - arcSize) / 2, (size.height - arcSize) / 2),
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center Info Ring Content (Active Selected Segment)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = activeCategory.categoryName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${(activeCategory.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        color = Color(activeCategory.colorHex)
                    )
                    Text(
                        text = formatRupiah(activeCategory.totalAmount),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action to view detailed pop-up for the active segment
            AssistChip(
                onClick = { onCategoryClick(activeCategory) },
                label = {
                    Text(
                        text = "Rincian Transaksi: ${activeCategory.categoryName}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(activeCategory.colorHex)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(activeCategory.colorHex).copy(alpha = 0.12f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("view_category_detail_chip")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Legend Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                spendingList.take(4).forEachIndexed { idx, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(item.colorHex),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.categoryName.take(12),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Belum Ada Pengeluaran Bulan Ini",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Catat pengeluaran belanja keluarga pertama Anda untuk melihat grafik interaktif.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}
