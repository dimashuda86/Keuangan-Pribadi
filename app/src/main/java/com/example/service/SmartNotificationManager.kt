package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import java.text.NumberFormat
import java.util.Locale

class SmartNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_BUDGET_ALERTS = "channel_budget_alerts"
        const val CHANNEL_FAMILY_SYNC = "channel_family_sync"
        const val CHANNEL_DAILY_REMINDER = "channel_daily_reminder"

        private var notificationIdCounter = 100
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Peringatan Anggaran Keluarga",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi saat kategori anggaran belanja mendekati 80% atau over-limit"
                enableVibration(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_FAMILY_SYNC,
                "Aktivitas Finansial Anggota",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pemberitahuan instan saat anggota keluarga mencatat pengeluaran atau pemasukan baru"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                "Pengingat Struk Harian (20.00)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat harian untuk mencatat struk belanja hari ini"
            }

            notificationManager.createNotificationChannels(listOf(budgetChannel, syncChannel, reminderChannel))
        }
    }

    fun showOverBudgetAlert(categoryName: String, percentage: Int, limit: Double, spent: Double) {
        if (!hasNotificationPermission()) return

        val formattedSpent = formatRupiah(spent)
        val formattedLimit = formatRupiah(limit)

        val isOverLimit = percentage >= 100
        val title = if (isOverLimit) {
            "🚨 PERINGATAN ANGGARAN MELEBIHI LIMIT!"
        } else {
            "⚠️ Peringatan: Anggaran $categoryName Capai $percentage%"
        }

        val message = if (isOverLimit) {
            "Pengeluaran kategori '$categoryName' telah mencapai $formattedSpent (Limit: $formattedLimit). Segera diskusikan dengan keluarga!"
        } else {
            "Pengeluaran '$categoryName' sudah mencapai $percentage% ($formattedSpent dari limit $formattedLimit). Kendalikan belanja!"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter++, builder.build())
        } catch (_: SecurityException) {
            // Handled gracefully
        }
    }

    fun showTransactionLoggedAlert(memberName: String, title: String, amount: Double, isExpense: Boolean) {
        if (!hasNotificationPermission()) return

        val formattedAmount = formatRupiah(amount)
        val notifTitle = if (isExpense) {
            "💸 $memberName mencatat pengeluaran"
        } else {
            "💰 $memberName mencatat pemasukan"
        }
        val content = "$title • $formattedAmount"

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_FAMILY_SYNC)
            .setSmallIcon(android.R.drawable.ic_input_add)
            .setContentTitle(notifTitle)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter++, builder.build())
        } catch (_: SecurityException) {
            // Handled gracefully
        }
    }

    fun showSyncBroadcastAlert(title: String, message: String) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_FAMILY_SYNC)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter++, builder.build())
        } catch (_: SecurityException) {
            // Handled gracefully
        }
    }

    fun showDailyReminder() {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDER)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("🧾 Rekap Pengeluaran Harian (20.00)")
            .setContentText("Ada struk belanja hari ini? Tap untuk foto & catat sekarang!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Simpan rapi keuangan keluarga kita setiap malam agar anggaran bulanan tetap terkontrol. Tap untuk scan atau input struk."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_camera, "Foto Struk", pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(999, builder.build())
        } catch (_: SecurityException) {
            // Handled gracefully
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }
}
