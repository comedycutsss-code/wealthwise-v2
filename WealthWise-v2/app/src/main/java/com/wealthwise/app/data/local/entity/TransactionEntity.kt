package com.wealthwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wealthwise.app.domain.model.Category
import com.wealthwise.app.domain.model.PaymentMode
import com.wealthwise.app.domain.model.TransactionType

/**
 * The raw, untouched SMS body is kept for auditing / re-parsing when the classifier improves.
 * This table lives in the same SQLCipher-encrypted database as everything else.
 */
@Entity(
    tableName = "raw_sms",
    indices = [Index(value = ["messageHash"], unique = true)]
)
data class RawSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val timestampMillis: Long,
    val messageHash: String, // sha-256 of sender+body+timestamp, used to dedupe re-scans
    val wasFinancial: Boolean,
    val processedAtMillis: Long
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = RawSmsEntity::class,
            parentColumns = ["id"],
            childColumns = ["rawSmsId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"]),
        Index(value = ["type"]),
        Index(value = ["merchant"]),
        Index(value = ["rawSmsId"]),
        Index(value = ["transactionId"], unique = false)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawSmsId: Long?,

    val date: Long,               // epoch millis, extracted from SMS body when present, else SMS receive time
    val amount: Double,
    val currency: String = "INR",

    val type: TransactionType,
    val category: Category,
    val paymentMode: PaymentMode,

    val bank: String?,
    val merchant: String?,
    val sender: String,

    val accountLast4: String?,
    val cardLast4: String?,
    val upiId: String?,

    val transactionId: String?,
    val referenceNumber: String?,
    val availableBalance: Double?,

    val description: String,

    val isRecurring: Boolean = false,
    val isDuplicateSuspect: Boolean = false,
    val confidenceScore: Float,   // 0f..1f — how sure the classifier is about this categorization

    val linkedLoanId: Long? = null,
    val linkedInvestmentId: Long? = null,
    val linkedCardId: Long? = null,

    val createdAtMillis: Long
)
