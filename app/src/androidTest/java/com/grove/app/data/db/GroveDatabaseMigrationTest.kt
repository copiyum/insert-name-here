package com.grove.app.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroveDatabaseMigrationTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            GroveDatabase::class.java,
            emptyList(),
        )

    @Test
    fun migrateAllSchemas() {
        helper.createDatabase("migrate-all", 1).close()

        helper.runMigrationsAndValidate("migrate-all", 5, true, *GroveDatabase.MIGRATIONS).close()
    }

    @Test
    fun migration4To5RemovesDeadTablesAndKeepsRows() {
        val categoryId = "11111111-1111-1111-1111-111111111111"
        val paymentMethodId = "22222222-2222-2222-2222-222222222222"
        val expenseId = "33333333-3333-3333-3333-333333333333"

        helper.createDatabase("migrate-4-5", 4).apply {
            execSQL(
                """
                INSERT INTO categories (
                    id, displayName, iconKey, colorHex, kind, sortOrder, archivedAt, createdAt, updatedAt
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(categoryId, "Food", "restaurant", "#A47148", "expense", 0, null, 1L, 1L),
            )
            execSQL(
                """
                INSERT INTO payment_methods (
                    id, displayName, kind, last4, colorHex, iconKey, sortOrder, archivedAt, createdAt, updatedAt
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(paymentMethodId, "Cash", "cash", null, "#A47148", "payments", 0, null, 1L, 1L),
            )
            execSQL(
                """
                INSERT INTO expenses (
                    id, amountMinor, currencyCode, categoryId, paymentMethodId, note, occurredAt, createdAt, updatedAt
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(expenseId, 1299L, "INR", categoryId, paymentMethodId, "lunch", 2L, 1L, 1L),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate("migrate-4-5", 5, true, *GroveDatabase.MIGRATIONS)

        assertFalse(db.hasColumn("categories", "colorHex"))
        assertFalse(db.hasColumn("expenses", "paymentMethodId"))
        assertFalse(db.hasTable("payment_methods"))
        assertEquals(1, db.longFor("SELECT COUNT(*) FROM categories").toInt())
        assertEquals("lunch", db.stringFor("SELECT note FROM expenses WHERE id = '$expenseId'"))

        db.close()
    }

    private fun SupportSQLiteDatabase.hasColumn(
        table: String,
        column: String,
    ): Boolean {
        query("PRAGMA table_info($table)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) return true
            }
        }
        return false
    }

    private fun SupportSQLiteDatabase.hasTable(table: String): Boolean =
        longFor("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '$table'") > 0

    private fun SupportSQLiteDatabase.longFor(sql: String): Long =
        query(sql).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getLong(0)
        }

    private fun SupportSQLiteDatabase.stringFor(sql: String): String =
        query(sql).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getString(0)
        }
}
