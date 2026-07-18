package com.wealthwise.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wealthwise.app.data.local.converter.Converters
import com.wealthwise.app.data.local.dao.TransactionDao
import com.wealthwise.app.data.local.entity.CreditCardEntity
import com.wealthwise.app.data.local.entity.InsurancePolicyEntity
import com.wealthwise.app.data.local.entity.InvestmentEntity
import com.wealthwise.app.data.local.entity.LoanEntity
import com.wealthwise.app.data.local.entity.RawSmsEntity
import com.wealthwise.app.data.local.entity.TransactionEntity
import com.wealthwise.app.security.DatabaseKeyProvider
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        TransactionEntity::class,
        RawSmsEntity::class,
        LoanEntity::class,
        CreditCardEntity::class,
        InvestmentEntity::class,
        InsurancePolicyEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WealthWiseDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DB_NAME = "wealthwise_encrypted.db"

        @Volatile private var instance: WealthWiseDatabase? = null

        /**
         * The passphrase is generated once, stored only as ciphertext (encrypted by an
         * Android Keystore key that never leaves the hardware-backed keystore), and never
         * written to disk or logs in plaintext. See [DatabaseKeyProvider].
         */
        fun getInstance(context: Context, keyProvider: DatabaseKeyProvider): WealthWiseDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context, keyProvider).also { instance = it }
            }
        }

        private fun build(context: Context, keyProvider: DatabaseKeyProvider): WealthWiseDatabase {
            SQLiteDatabase.loadLibs(context)
            val passphrase = keyProvider.getOrCreateDatabasePassphrase()
            val factory = SupportFactory(passphrase)

            return Room.databaseBuilder(context.applicationContext, WealthWiseDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration() // acceptable pre-1.0; replace with real migrations before release
                .build()
        }
    }
}
