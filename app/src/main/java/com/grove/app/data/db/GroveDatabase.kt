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
import com.grove.app.data.db.dao.PaymentMethodDao
import com.grove.app.data.db.dao.UserProfileDao
import com.grove.app.data.db.entity.BillEntity
import com.grove.app.data.db.entity.BillPaymentEntity
import com.grove.app.data.db.entity.CategoryEntity
import com.grove.app.data.db.entity.ExpenseEntity
import com.grove.app.data.db.entity.IncomeEntity
import com.grove.app.data.db.entity.MonthlyBudgetEntity
import com.grove.app.data.db.entity.MonthlyCategoryBudgetEntity
import com.grove.app.data.db.entity.NotificationSettingsEntity
import com.grove.app.data.db.entity.PaymentMethodEntity
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
        PaymentMethodEntity::class,
        NotificationSettingsEntity::class,
        ScheduledNotificationEntity::class,
    ],
    version = 4,
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

    abstract fun paymentMethodDao(): PaymentMethodDao

    abstract fun notificationDao(): NotificationDao

    companion object {
        fun build(context: Context): GroveDatabase {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            lateinit var instance: GroveDatabase
            instance =
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        GroveDatabase::class.java,
                        "grove.db",
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
    }
}
