package com.example.solosale.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.solosale.data.local.dao.CustomerDao
import com.example.solosale.data.local.dao.PaymentDao
import com.example.solosale.data.local.dao.ProductDao
import com.example.solosale.data.local.dao.PurchaseDao
import com.example.solosale.data.local.dao.PurchaseItemDao
import com.example.solosale.data.local.dao.SaleDao
import com.example.solosale.data.local.dao.SaleItemDao
import com.example.solosale.data.local.dao.SessionDao
import com.example.solosale.data.local.dao.SettingsDao
import com.example.solosale.data.local.dao.StockAdjustmentDao
import com.example.solosale.data.local.dao.SupplierDao
import com.example.solosale.data.local.dao.UserDao
import com.example.solosale.data.local.entity.BusinessSettingsEntity
import com.example.solosale.data.local.entity.CustomerEntity
import com.example.solosale.data.local.entity.PaymentEntity
import com.example.solosale.data.local.entity.ProductEntity
import com.example.solosale.data.local.entity.PurchaseEntity
import com.example.solosale.data.local.entity.PurchaseItemEntity
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.local.entity.SaleItemEntity
import com.example.solosale.data.local.entity.SessionEntity
import com.example.solosale.data.local.entity.StockAdjustmentEntity
import com.example.solosale.data.local.entity.SupplierEntity
import com.example.solosale.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        SessionEntity::class,
        ProductEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        PaymentEntity::class,
        StockAdjustmentEntity::class,
        BusinessSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solosale_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initialize default settings on background thread
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.settingsDao()?.insertOrUpdate(BusinessSettingsEntity())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
