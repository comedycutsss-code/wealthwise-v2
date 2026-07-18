package com.wealthwise.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wealthwise.app.domain.model.Category

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lender: String,
    val loanType: Category,
    val accountLast4: String?,
    val principalAmount: Double?,
    val outstandingBalance: Double?,
    val emiAmount: Double?,
    val interestRate: Double?,
    val nextEmiDate: Long?,
    val startDate: Long?,
    val isClosed: Boolean = false,
    val closedDate: Long? = null,
    val lastUpdatedMillis: Long
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val issuer: String,
    val cardLast4: String,
    val creditLimit: Double?,
    val availableLimit: Double?,
    val currentOutstanding: Double?,
    val minimumDue: Double?,
    val billGeneratedDate: Long?,
    val billDueDate: Long?,
    val rewardPoints: Int?,
    val lastUpdatedMillis: Long
)

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platform: String,               // Groww, Zerodha, CAMS, etc.
    val instrumentType: Category,       // MUTUAL_FUND, STOCKS, FIXED_DEPOSIT, ...
    val name: String,                   // fund name / stock symbol / FD account
    val folioOrAccountNumber: String?,
    val isSip: Boolean = false,
    val sipAmount: Double? = null,
    val unitsHeld: Double? = null,
    val avgNav: Double? = null,
    val totalInvested: Double = 0.0,
    val currentValue: Double? = null,
    val maturityDate: Long? = null,
    val interestRate: Double? = null,
    val lastUpdatedMillis: Long
)

@Entity(tableName = "insurance_policies")
data class InsurancePolicyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val insurer: String,
    val policyNumber: String?,
    val policyType: String?,
    val premiumAmount: Double?,
    val premiumFrequency: String?, // MONTHLY / QUARTERLY / YEARLY
    val nextDueDate: Long?,
    val sumAssured: Double?,
    val lastUpdatedMillis: Long
)
