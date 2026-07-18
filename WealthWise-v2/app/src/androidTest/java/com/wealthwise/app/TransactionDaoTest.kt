package com.wealthwise.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.wealthwise.app.data.local.WealthWiseDatabase
import com.wealthwise.app.data.local.entity.TransactionEntity
import com.wealthwise.app.domain.model.Category
import com.wealthwise.app.domain.model.PaymentMode
import com.wealthwise.app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var db: WealthWiseDatabase

    @Before
    fun setUp() {
        // In-memory, unencrypted build used purely for instrumentation testing of query logic;
        // production always goes through WealthWiseDatabase.getInstance() with SQLCipher.
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WealthWiseDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndObserveIncomeBetween() = runBlocking {
        val now = System.currentTimeMillis()
        db.transactionDao().insertTransaction(
            TransactionEntity(
                rawSmsId = null,
                date = now,
                amount = 45000.0,
                type = TransactionType.INCOME,
                category = Category.SALARY,
                paymentMode = PaymentMode.NEFT,
                bank = "HDFC Bank",
                merchant = null,
                sender = "VM-HDFCBK",
                accountLast4 = "4521",
                cardLast4 = null,
                upiId = null,
                transactionId = null,
                referenceNumber = null,
                availableBalance = 68540.0,
                description = "Salary credit",
                confidenceScore = 0.95f,
                createdAtMillis = now
            )
        )

        val income = db.transactionDao().observeIncomeBetween(now - 1000, now + 1000).first()
        assertThat(income).isEqualTo(45000.0)
    }

    @Test
    fun searchFindsTransactionByMerchant() = runBlocking {
        val now = System.currentTimeMillis()
        db.transactionDao().insertTransaction(
            TransactionEntity(
                rawSmsId = null, date = now, amount = 850.0, type = TransactionType.EXPENSE,
                category = Category.FOOD, paymentMode = PaymentMode.UPI, bank = "ICICI Bank",
                merchant = "SWIGGY", sender = "AD-ICICIB", accountLast4 = "7788", cardLast4 = null,
                upiId = null, transactionId = "302981123456", referenceNumber = null,
                availableBalance = 67690.0, description = "Food order", confidenceScore = 0.9f,
                createdAtMillis = now
            )
        )

        val results = db.transactionDao().search("SWIGGY").first()
        assertThat(results).hasSize(1)
        assertThat(results.first().merchant).isEqualTo("SWIGGY")
    }
}
