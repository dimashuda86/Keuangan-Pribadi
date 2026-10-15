package com.example.service

import android.content.Context
import android.content.Intent
import com.example.domain.model.BudgetItem
import com.example.domain.model.MonthlySummary
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportService(private val context: Context) {

    fun generateCsv(transactions: List<TransactionItem>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("id", "ID"))
        val sb = StringBuilder()
        sb.appendLine("ID,Tanggal,Tipe,Judul,Kategori,Nominal(IDR),DicatatOleh,Peran,Catatan")

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.timestamp))
            val cleanTitle = tx.title.replace(",", " ")
            val cleanCat = tx.categoryName.replace(",", " ")
            val cleanMember = tx.memberName.replace(",", " ")
            val cleanNote = tx.note.replace(",", " ").replace("\n", " ")
            val typeStr = if (tx.type == TransactionType.EXPENSE) "PENGELUARAN" else "PEMASUKAN"

            sb.appendLine("${tx.id},$dateStr,$typeStr,$cleanTitle,$cleanCat,${tx.amount.toLong()},$cleanMember,${tx.memberRole},$cleanNote")
        }
        return sb.toString()
    }

    fun generateMonthlyReportSummary(
        summary: MonthlySummary,
        budgets: List<BudgetItem>,
        transactions: List<TransactionItem>
    ): String {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        val currentMonth = dateFormat.format(Date())

        return buildString {
            appendLine("📊 *REKAPITULASI KEUANGAN KELUARGA*")
            appendLine("Periode: $currentMonth")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("💵 Total Pemasukan: ${formatRupiah(summary.totalIncome)}")
            appendLine("💸 Total Pengeluaran: ${formatRupiah(summary.totalExpense)}")
            appendLine("💰 Sisa Saldo: ${formatRupiah(summary.balance)}")
            appendLine("📈 Rasio Tabungan: ${summary.savingsRate}%")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🎯 *STATUS ANGGARAN KATEGORI:*")
            budgets.forEach { b ->
                val emoji = when {
                    b.percentage >= 100 -> "🚨 [OVERLIMIT]"
                    b.percentage >= 80 -> "⚠️ [PERINGATAN]"
                    else -> "✅ [AMAN]"
                }
                appendLine("• ${b.categoryName}: ${formatRupiah(b.currentSpent)} / ${formatRupiah(b.monthlyLimit)} (${b.percentage}%) $emoji")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Total Transaksi Terverifikasi: ${transactions.size}")
            appendLine("Laporan di-generate otomatis oleh CatatUang Keluarga")
        }
    }

    fun shareExportText(content: String, title: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Ekspor Laporan Keuangan"))
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }
}
