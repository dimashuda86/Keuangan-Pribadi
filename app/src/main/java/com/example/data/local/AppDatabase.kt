package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.FamilyGroupDao
import com.example.data.local.dao.FamilyMemberDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.WalletAccountDao
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyGroupEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletAccountEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FamilyMemberEntity::class,
        FamilyGroupEntity::class,
        WalletAccountEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        TransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun walletAccountDao(): WalletAccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun familyGroupDao(): FamilyGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "family_finance.db"
                ).fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (instance.familyMemberDao().getMemberCount() == 0) {
                            instance.seedInitialDataInternal()
                        }
                    } catch (_: Exception) {}
                }
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database)
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database)
                }
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            db.seedInitialDataInternal()
        }
    }

    suspend fun seedInitialDataInternal() {
        val db = this
            // 1. Seed Family Group
            db.familyGroupDao().insertFamilyGroup(
                FamilyGroupEntity(
                    id = "vault_main",
                    name = "Keluarga Cemara Finansial",
                    pairingCode = "FAM-8921",
                    currency = "IDR",
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            )

            // 2. Seed Family Members (Multi-user roles)
            val members = listOf(
                FamilyMemberEntity("mem_ayah", "Ayah (Budi)", "Kepala Keluarga", 0xFF2563EB),
                FamilyMemberEntity("mem_ibu", "Ibu (Siti)", "Bendahara Keluarga", 0xFFDB2777),
                FamilyMemberEntity("mem_anak1", "Kakak (Rian)", "Anak Pertama (SMA)", 0xFF059669),
                FamilyMemberEntity("mem_anak2", "Adik (Nisa)", "Anak Kedua (SMP)", 0xFFEA580C)
            )
            db.familyMemberDao().insertMembers(members)

            // 3. Seed Multi-Accounts (Beberapa Akun Finansial)
            val wallets = listOf(
                WalletAccountEntity("acc_bca", "BCA Prioritas Ayah", "BANK", "•••• 7812", 18500000.0, 0xFF005E9E, "mem_ayah", "Ayah (Budi)"),
                WalletAccountEntity("acc_cash", "Dompet Tunai / Cash Ibu", "CASH", "Uang Fisik", 3200000.0, 0xFF0D9488, "mem_ibu", "Ibu (Siti)"),
                WalletAccountEntity("acc_gopay", "GoPay / OVO Keluarga", "EWALLET", "0812-3456-7890", 850000.0, 0xFF00AED6, "mem_ibu", "Ibu (Siti)"),
                WalletAccountEntity("acc_mandiri", "Tabungan Bersama Mandiri", "SAVINGS", "•••• 9021", 45000000.0, 0xFFF59E0B, "mem_ayah", "Keluarga Cemara"),
                WalletAccountEntity("acc_jago", "Uang Saku Kakak (Jago)", "EWALLET", "•••• 3341", 600000.0, 0xFF8B5CF6, "mem_anak1", "Kakak (Rian)")
            )
            db.walletAccountDao().insertWallets(wallets)

            // 4. Seed Categories
            val categories = listOf(
                CategoryEntity("cat_dapur", "Kebutuhan Dapur & Sembako", "EXPENSE", "ShoppingBag", 0xFF0D9488),
                CategoryEntity("cat_makan", "Makan & Kuliner Keluarga", "EXPENSE", "Restaurant", 0xFFF59E0B),
                CategoryEntity("cat_transport", "Transportasi & Bensin", "EXPENSE", "DirectionsCar", 0xFF3B82F6),
                CategoryEntity("cat_edukasi", "Pendidikan & Buku Anak", "EXPENSE", "School", 0xFF8B5CF6),
                CategoryEntity("cat_tagihan", "Tagihan Listrik, Air & WiFi", "EXPENSE", "FlashOn", 0xFFEF4444),
                CategoryEntity("cat_kesehatan", "Kesehatan & Obat", "EXPENSE", "LocalHospital", 0xFF10B981),
                CategoryEntity("cat_hiburan", "Hiburan & Liburan", "EXPENSE", "Celebration", 0xFFEC4899),
                CategoryEntity("cat_gaji", "Gaji Pokok Ayah & Ibu", "INCOME", "AccountBalanceWallet", 0xFF10B981),
                CategoryEntity("cat_bonus", "Bonus & Usaha Sampingan", "INCOME", "TrendingUp", 0xFF059669),
                CategoryEntity("cat_transfer", "Transfer Antar Dompet", "TRANSFER", "SwapHoriz", 0xFF6366F1)
            )
            db.categoryDao().insertCategories(categories)

            // 5. Seed Budgets
            val budgets = listOf(
                BudgetEntity("b_dapur", "cat_dapur", "Kebutuhan Dapur & Sembako", 3500000.0, 80),
                BudgetEntity("b_makan", "cat_makan", "Makan & Kuliner Keluarga", 2200000.0, 80),
                BudgetEntity("b_transport", "cat_transport", "Transportasi & Bensin", 1200000.0, 80),
                BudgetEntity("b_tagihan", "cat_tagihan", "Tagihan Listrik, Air & WiFi", 1100000.0, 85),
                BudgetEntity("b_edukasi", "cat_edukasi", "Pendidikan & Buku Anak", 1500000.0, 80),
                BudgetEntity("b_hiburan", "cat_hiburan", "Hiburan & Liburan", 800000.0, 75)
            )
            db.budgetDao().insertBudgets(budgets)

            // 6. Seed Savings Goals
            val goals = listOf(
                SavingsGoalEntity("g_darurat", "Dana Darurat Keluarga", 50000000.0, 32500000.0, "Des 2026", "🛡️", 0xFF0D9488),
                SavingsGoalEntity("g_liburan", "Liburan Akhir Tahun ke Bali", 15000000.0, 11200000.0, "Nov 2026", "🏖️", 0xFFF59E0B),
                SavingsGoalEntity("g_pendidikan", "Dana Masuk Kuliah Kakak", 30000000.0, 18000000.0, "Jun 2027", "🎓", 0xFF8B5CF6)
            )
            db.savingsGoalDao().insertGoals(goals)

            // 7. Seed Initial Transactions
            val now = System.currentTimeMillis()
            val day = 86400000L
            val transactions = listOf(
                TransactionEntity(
                    title = "Gaji Bulanan Masuk",
                    amount = 14500000.0,
                    type = "INCOME",
                    categoryId = "cat_gaji",
                    categoryName = "Gaji Pokok Ayah & Ibu",
                    memberId = "mem_ayah",
                    memberName = "Ayah (Budi)",
                    memberRole = "Kepala Keluarga",
                    walletId = "acc_bca",
                    walletName = "BCA Prioritas Ayah",
                    note = "Transfer payroll bulanan awal bulan",
                    timestamp = now - (6 * day)
                ),
                TransactionEntity(
                    title = "Belanja Bulanan Supermarket",
                    amount = 2950000.0,
                    type = "EXPENSE",
                    categoryId = "cat_dapur",
                    categoryName = "Kebutuhan Dapur & Sembako",
                    memberId = "mem_ibu",
                    memberName = "Ibu (Siti)",
                    memberRole = "Bendahara Keluarga",
                    walletId = "acc_cash",
                    walletName = "Dompet Tunai / Cash Ibu",
                    note = "Minyak, beras, sayur, deterjen, susu",
                    timestamp = now - (4 * day)
                ),
                TransactionEntity(
                    title = "Tarik Tunai untuk Belanja Pasar",
                    amount = 2000000.0,
                    type = "TRANSFER",
                    categoryId = "cat_transfer",
                    categoryName = "Transfer Antar Dompet",
                    memberId = "mem_ayah",
                    memberName = "Ayah (Budi)",
                    memberRole = "Kepala Keluarga",
                    walletId = "acc_bca",
                    walletName = "BCA Prioritas Ayah",
                    destinationWalletId = "acc_cash",
                    destinationWalletName = "Dompet Tunai / Cash Ibu",
                    note = "Ambil uang fisik di ATM BCA",
                    timestamp = now - (4 * day + 3600000L)
                ),
                TransactionEntity(
                    title = "Makan Siang Bersama Akhir Pekan",
                    amount = 650000.0,
                    type = "EXPENSE",
                    categoryId = "cat_makan",
                    categoryName = "Makan & Kuliner Keluarga",
                    memberId = "mem_ayah",
                    memberName = "Ayah (Budi)",
                    memberRole = "Kepala Keluarga",
                    walletId = "acc_bca",
                    walletName = "BCA Prioritas Ayah",
                    note = "Restoran Sunda sekeluarga (Debit BCA)",
                    timestamp = now - (3 * day)
                ),
                TransactionEntity(
                    title = "Buku Paket Ujian & LKS",
                    amount = 450000.0,
                    type = "EXPENSE",
                    categoryId = "cat_edukasi",
                    categoryName = "Pendidikan & Buku Anak",
                    memberId = "mem_anak1",
                    memberName = "Kakak (Rian)",
                    memberRole = "Anak Pertama (SMA)",
                    walletId = "acc_jago",
                    walletName = "Uang Saku Kakak (Jago)",
                    note = "Buku latihan persiapan semester",
                    timestamp = now - (2 * day)
                ),
                TransactionEntity(
                    title = "Token Listrik PLN & IndiHome",
                    amount = 850000.0,
                    type = "EXPENSE",
                    categoryId = "cat_tagihan",
                    categoryName = "Tagihan Listrik, Air & WiFi",
                    memberId = "mem_ayah",
                    memberName = "Ayah (Budi)",
                    memberRole = "Kepala Keluarga",
                    walletId = "acc_gopay",
                    walletName = "GoPay / OVO Keluarga",
                    note = "Listrik 500k + wifi 350k bayar via GoTagihan",
                    timestamp = now - day
                ),
                TransactionEntity(
                    title = "Bensin Motor & Service Ringan",
                    amount = 320000.0,
                    type = "EXPENSE",
                    categoryId = "cat_transport",
                    categoryName = "Transportasi & Bensin",
                    memberId = "mem_anak1",
                    memberName = "Kakak (Rian)",
                    memberRole = "Anak Pertama (SMA)",
                    walletId = "acc_cash",
                    walletName = "Dompet Tunai / Cash Ibu",
                    note = "Pertamax isi full & ganti oli",
                    timestamp = now - (12 * 3600000L)
                )
            )

            transactions.forEach { db.transactionDao().insertTransaction(it) }
    }
}
