package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BotShareService(private val context: Context) {

    fun shareTransactionToTelegramOrWhatsApp(
        transaction: TransactionItem,
        useWhatsApp: Boolean = true
    ) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(transaction.timestamp))
        val typeLabel = if (transaction.type == TransactionType.EXPENSE) "🔴 PENGELUARAN" else "🟢 PEMASUKAN"
        val formattedAmount = formatRupiah(transaction.amount)

        val message = buildString {
            appendLine("🧾 *LAPORAN KEUANGAN KELUARGA*")
            appendLine("═════════════════════")
            appendLine("Tipe: $typeLabel")
            appendLine("Judul: *${transaction.title}*")
            appendLine("Nominal: *$formattedAmount*")
            appendLine("Kategori: ${transaction.categoryName}")
            appendLine("Dicatat Oleh: ${transaction.memberName} (${transaction.memberRole})")
            appendLine("Waktu: $dateStr")
            if (transaction.note.isNotBlank()) {
                appendLine("Catatan: ${transaction.note}")
            }
            appendLine("═════════════════════")
            appendLine("🤖 _Terkirim otomatis via CatatUang Keluarga Bot Sync_")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            if (useWhatsApp) {
                setPackage("com.whatsapp")
            } else {
                setPackage("org.telegram.messenger")
            }
        }

        try {
            val chooser = Intent.createChooser(sendIntent, "Kirim via Bot Finansial")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Fallback to general intent if specific app package is not installed
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Bagikan Laporan Finansial"))
        }
    }

    fun openTelegramBotWebhookGuide(): Intent {
        val url = "https://t.me/FamilyFinanceBot"
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }
}
