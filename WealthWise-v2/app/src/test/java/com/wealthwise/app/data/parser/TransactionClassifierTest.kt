package com.wealthwise.app.data.parser

import com.google.common.truth.Truth.assertThat
import com.wealthwise.app.domain.model.Category
import com.wealthwise.app.domain.model.TransactionType
import org.junit.Test

class TransactionClassifierTest {

    @Test
    fun `classifies salary credit as income`() {
        val result = TransactionClassifier.classify("Rs.45000.00 credited to A/c XX4521. Salary - Infotech Pvt Ltd")
        assertThat(result.category).isEqualTo(Category.SALARY)
        assertThat(result.type).isEqualTo(TransactionType.INCOME)
        assertThat(result.confidence).isGreaterThan(0.8f)
    }

    @Test
    fun `classifies UPI food delivery spend as expense food`() {
        val result = TransactionClassifier.classify("Rs.850.00 debited to SWIGGY via UPI")
        assertThat(result.category).isEqualTo(Category.FOOD)
        assertThat(result.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `classifies SIP as mutual fund investment`() {
        val result = TransactionClassifier.classify("SIP of Rs.5000.00 in Parag Parikh Flexi Cap Fund processed. Units allotted")
        assertThat(result.category).isEqualTo(Category.MUTUAL_FUND)
    }

    @Test
    fun `rejects OTP message as non-financial`() {
        val isFinancial = TransactionClassifier.isLikelyFinancial("Your OTP is 482913. Do not share this with anyone.")
        assertThat(isFinancial).isFalse()
    }

    @Test
    fun `rejects promotional message as non-financial`() {
        val isFinancial = TransactionClassifier.isLikelyFinancial("Huge Sale! Flat 70% off on electronics. Click here to shop now.")
        assertThat(isFinancial).isFalse()
    }

    @Test
    fun `accepts EMI message as financial`() {
        val isFinancial = TransactionClassifier.isLikelyFinancial("Rs.12000.00 debited towards Home Loan EMI from A/c XX9021")
        assertThat(isFinancial).isTrue()
    }
}
