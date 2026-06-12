package com.grove.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.grove.app.data.db.dao.BillDao
import com.grove.app.data.db.dao.BillPaymentDao
import com.grove.app.data.db.dao.CategoryDao
import com.grove.app.data.db.dao.ExpenseDao
import com.grove.app.data.db.dao.IncomeDao
import com.grove.app.data.db.dao.MonthlyBudgetDao
import com.grove.app.data.db.dao.NotificationDao
import com.grove.app.data.db.dao.UserProfileDao
import com.grove.app.data.db.entity.BillEntity
import com.grove.app.data.db.entity.BillPaymentEntity
import com.grove.app.data.db.entity.CategoryEntity
import com.grove.app.data.db.entity.ExpenseEntity
import com.grove.app.data.db.entity.IncomeEntity
import com.grove.app.data.db.entity.MonthlyBudgetEntity
import com.grove.app.data.db.entity.MonthlyCategoryBudgetEntity
import com.grove.app.data.db.entity.NotificationSettingsEntity
import com.grove.app.data.db.entity.ScheduledNotificationEntity
import com.grove.app.data.db.entity.UserProfileEntity
import com.grove.app.data.db.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        BillEntity::class,
        BillPaymentEntity::class,
        IncomeEntity::class,
        MonthlyBudgetEntity::class,
        MonthlyCategoryBudgetEntity::class,
        NotificationSettingsEntity::class,
        ScheduledNotificationEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GroveDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    abstract fun categoryDao(): CategoryDao

    abstract fun expenseDao(): ExpenseDao

    abstract fun billDao(): BillDao

    abstract fun billPaymentDao(): BillPaymentDao

    abstract fun incomeDao(): IncomeDao

    abstract fun monthlyBudgetDao(): MonthlyBudgetDao

    abstract fun notificationDao(): NotificationDao

    companion object {
        val MIGRATIONS: Array<Migration>
            get() = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)

        fun build(context: Context): GroveDatabase {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            lateinit var instance: GroveDatabase
            instance =
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        GroveDatabase::class.java,
                        "grove.db",
                    ).addMigrations(*MIGRATIONS)
                    .addCallback(
                        object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                scope.launch { SeedData.seed(instance) }
                            }
                        },
                    ).build()
            return instance
        }

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_monthly_category_budgets_categoryId ON monthly_category_budgets(categoryId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_notifications_relatedBillId ON scheduled_notifications(relatedBillId)")
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_expenses_fts_BEFORE_UPDATE")
                    db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_expenses_fts_BEFORE_DELETE")
                    db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_expenses_fts_AFTER_UPDATE")
                    db.execSQL("DROP TRIGGER IF EXISTS room_fts_content_sync_expenses_fts_AFTER_INSERT")
                    db.execSQL("DROP TABLE IF EXISTS expenses_fts")
                }
            }

        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("DROP TABLE IF EXISTS expense_tags")
                    db.execSQL("DROP TABLE IF EXISTS income_tags")
                    db.execSQL("DROP TABLE IF EXISTS tags")
                }
            }

        private val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS categories_new (
                            id TEXT NOT NULL,
                            displayName TEXT NOT NULL,
                            iconKey TEXT NOT NULL,
                            kind TEXT NOT NULL,
                            sortOrder INTEGER NOT NULL,
                            archivedAt INTEGER,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            PRIMARY KEY(id)
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO categories_new (id, displayName, iconKey, kind, sortOrder, archivedAt, createdAt, updatedAt)
                        SELECT id, displayName, iconKey, kind, sortOrder, archivedAt, createdAt, updatedAt FROM categories
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS expenses_v5_data (
                            id TEXT NOT NULL,
                            amountMinor INTEGER NOT NULL,
                            currencyCode TEXT NOT NULL,
                            categoryId TEXT NOT NULL,
                            note TEXT NOT NULL,
                            occurredAt INTEGER NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            PRIMARY KEY(id)
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO expenses_v5_data (id, amountMinor, currencyCode, categoryId, note, occurredAt, createdAt, updatedAt)
                        SELECT id, amountMinor, currencyCode, categoryId, note, occurredAt, createdAt, updatedAt FROM expenses
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS incomes_v5_data (
                            id TEXT NOT NULL,
                            amountMinor INTEGER NOT NULL,
                            currencyCode TEXT NOT NULL,
                            source TEXT NOT NULL,
                            categoryId TEXT NOT NULL,
                            occurredAt INTEGER NOT NULL,
                            isRecurring INTEGER NOT NULL,
                            recurrenceFrequency TEXT,
                            nextExpectedAt INTEGER,
                            note TEXT NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            PRIMARY KEY(id)
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO incomes_v5_data (
                            id, amountMinor, currencyCode, source, categoryId, occurredAt, isRecurring,
                            recurrenceFrequency, nextExpectedAt, note, createdAt, updatedAt
                        )
                        SELECT
                            id, amountMinor, currencyCode, source, categoryId, occurredAt, isRecurring,
                            recurrenceFrequency, nextExpectedAt, note, createdAt, updatedAt
                        FROM incomes
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS monthly_category_budgets_v5_data (
                            id TEXT NOT NULL,
                            monthlyBudgetId TEXT NOT NULL,
                            categoryId TEXT NOT NULL,
                            amountMinor INTEGER NOT NULL,
                            PRIMARY KEY(id)
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO monthly_category_budgets_v5_data (id, monthlyBudgetId, categoryId, amountMinor)
                        SELECT id, monthlyBudgetId, categoryId, amountMinor FROM monthly_category_budgets
                        """.trimIndent(),
                    )
                    db.execSQL("DROP TABLE expenses")
                    db.execSQL("DROP TABLE incomes")
                    db.execSQL("DROP TABLE monthly_category_budgets")
                    db.execSQL("DROP TABLE categories")
                    db.execSQL("DROP TABLE IF EXISTS payment_methods")
                    db.execSQL("ALTER TABLE categories_new RENAME TO categories")
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS expenses (
                            id TEXT NOT NULL,
                            amountMinor INTEGER NOT NULL,
                            currencyCode TEXT NOT NULL,
                            categoryId TEXT NOT NULL,
                            note TEXT NOT NULL,
                            occurredAt INTEGER NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            PRIMARY KEY(id),
                            FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO expenses (id, amountMinor, currencyCode, categoryId, note, occurredAt, createdAt, updatedAt)
                        SELECT id, amountMinor, currencyCode, categoryId, note, occurredAt, createdAt, updatedAt FROM expenses_v5_data
                        """.trimIndent(),
                    )
                    db.execSQL("DROP TABLE expenses_v5_data")
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS incomes (
                            id TEXT NOT NULL,
                            amountMinor INTEGER NOT NULL,
                            currencyCode TEXT NOT NULL,
                            source TEXT NOT NULL,
                            categoryId TEXT NOT NULL,
                            occurredAt INTEGER NOT NULL,
                            isRecurring INTEGER NOT NULL,
                            recurrenceFrequency TEXT,
                            nextExpectedAt INTEGER,
                            note TEXT NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL,
                            PRIMARY KEY(id),
                            FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO incomes (
                            id, amountMinor, currencyCode, source, categoryId, occurredAt, isRecurring,
                            recurrenceFrequency, nextExpectedAt, note, createdAt, updatedAt
                        )
                        SELECT
                            id, amountMinor, currencyCode, source, categoryId, occurredAt, isRecurring,
                            recurrenceFrequency, nextExpectedAt, note, createdAt, updatedAt
                        FROM incomes_v5_data
                        """.trimIndent(),
                    )
                    db.execSQL("DROP TABLE incomes_v5_data")
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS monthly_category_budgets (
                            id TEXT NOT NULL,
                            monthlyBudgetId TEXT NOT NULL,
                            categoryId TEXT NOT NULL,
                            amountMinor INTEGER NOT NULL,
                            PRIMARY KEY(id),
                            FOREIGN KEY(monthlyBudgetId) REFERENCES monthly_budgets(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                            FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO monthly_category_budgets (id, monthlyBudgetId, categoryId, amountMinor)
                        SELECT id, monthlyBudgetId, categoryId, amountMinor FROM monthly_category_budgets_v5_data
                        """.trimIndent(),
                    )
                    db.execSQL("DROP TABLE monthly_category_budgets_v5_data")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_kind_archivedAt ON categories(kind, archivedAt)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_occurredAt ON expenses(occurredAt)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_categoryId ON expenses(categoryId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_occurredAt_categoryId ON expenses(occurredAt, categoryId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_incomes_occurredAt ON incomes(occurredAt)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_incomes_categoryId ON incomes(categoryId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_incomes_isRecurring_nextExpectedAt ON incomes(isRecurring, nextExpectedAt)")
                    db.execSQL(
                        """
                        CREATE UNIQUE INDEX IF NOT EXISTS index_monthly_category_budgets_monthlyBudgetId_categoryId
                        ON monthly_category_budgets(monthlyBudgetId, categoryId)
                        """.trimIndent(),
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_monthly_category_budgets_categoryId ON monthly_category_budgets(categoryId)")
                }
            }
    }
}
