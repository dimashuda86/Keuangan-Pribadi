package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.BudgetItem
import com.example.domain.model.BudgetAlertStatus
import com.example.domain.model.FamilyMember
import com.example.domain.model.SavingsGoal
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.WalletAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CatatUang Keluarga", appName)
    }

    @Test
    fun `test budget alert threshold calculation`() {
        val budgetSafe = BudgetItem("b1", "cat1", "Dapur", 1000000.0, 500000.0, 80)
        assertEquals(BudgetAlertStatus.SAFE, budgetSafe.alertStatus)

        val budgetWarning = BudgetItem("b2", "cat2", "Dapur", 1000000.0, 850000.0, 80)
        assertEquals(BudgetAlertStatus.WARNING, budgetWarning.alertStatus)

        val budgetOver = BudgetItem("b3", "cat3", "Dapur", 1000000.0, 1100000.0, 80)
        assertEquals(BudgetAlertStatus.OVER_LIMIT, budgetOver.alertStatus)
    }

    @Test
    fun `test savings goal edit and percentage calculation`() {
        val initialGoal = SavingsGoal("g1", "Liburan Bali", 10000000.0, 2000000.0, "Des 2026", "🏖️")
        assertEquals(20, initialGoal.percentage)

        // User edits savings goal: deposits more and updates target
        val editedGoal = initialGoal.copy(currentAmount = 5000000.0, targetAmount = 10000000.0)
        assertEquals(50, editedGoal.percentage)
        assertEquals(0.5f, editedGoal.progress)
    }

    @Test
    fun `test wallet account edit and balance reset`() {
        val initialWallet = WalletAccount(
            id = "acc_gopay",
            name = "GoPay Ayah",
            type = "EWALLET",
            accountNumber = "08123456789",
            balance = 500000.0,
            colorHex = 0xFF00AED6,
            holderMemberId = "mem_ayah",
            holderName = "Ayah"
        )
        assertEquals("EWALLET", initialWallet.type)
        assertEquals(500000.0, initialWallet.balance, 0.01)

        // User edits e-wallet name & balance
        val editedWallet = initialWallet.copy(name = "GoPay Prioritas", balance = 750000.0)
        assertEquals("GoPay Prioritas", editedWallet.name)
        assertEquals(750000.0, editedWallet.balance, 0.01)

        // Reset wallet balance to 0
        val resetWallet = editedWallet.copy(balance = 0.0)
        assertEquals(0.0, resetWallet.balance, 0.01)
    }

    @Test
    fun `test receipt transaction edit functionality`() {
        val tx = TransactionItem(
            id = 101,
            title = "Superindo",
            amount = 150000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_dapur",
            categoryName = "Kebutuhan Dapur",
            memberId = "mem_ayah",
            memberName = "Ayah",
            memberRole = "Kepala Keluarga",
            note = "Minyak goreng, telur"
        )

        // User edits receipt details
        val updatedReceipt = tx.copy(
            title = "Superindo Mall",
            amount = 185000.0,
            note = "Minyak goreng, telur, susu UHT 1L"
        )

        assertEquals("Superindo Mall", updatedReceipt.title)
        assertEquals(185000.0, updatedReceipt.amount, 0.01)
        assertTrue(updatedReceipt.note.contains("susu UHT"))
    }

    @Test
    fun `test camera permission in manifest`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        assertNotNull(packageInfo.requestedPermissions)
        assertTrue(packageInfo.requestedPermissions!!.contains(Manifest.permission.CAMERA))
    }

    @Test
    fun `test family member edit name and delete validation`() {
        val member = FamilyMember(
            id = "mem_ayah",
            name = "Ayah (Budi)",
            role = "Kepala Keluarga",
            colorHex = 0xFF2563EB
        )
        assertEquals("A", member.avatarInitial)

        // User edits member name and role
        val editedMember = member.copy(
            name = "Budi Santoso",
            role = "Ayah & Kepala Keluarga"
        )
        assertEquals("Budi Santoso", editedMember.name)
        assertEquals("Ayah & Kepala Keluarga", editedMember.role)
        assertEquals("B", editedMember.avatarInitial)

        // Test delete member list count guard: minimum 1 member required
        val initialMembers = listOf(editedMember)
        val canDelete = initialMembers.size > 1
        assertEquals(false, canDelete)

        val multipleMembers = listOf(
            editedMember,
            FamilyMember("mem_ibu", "Ibu (Siti)", "Bendahara Keluarga", 0xFFDB2777)
        )
        assertEquals(true, multipleMembers.size > 1)
        val afterDeletion = multipleMembers.filter { it.id != "mem_ibu" }
        assertEquals(1, afterDeletion.size)
        assertEquals("Budi Santoso", afterDeletion[0].name)
    }
}
