package com.wealthwise.app.data.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmsFieldExtractorTest {

    @Test
    fun `extracts amount with Rs prefix and commas`() {
        val amount = SmsFieldExtractor.extractAmount("Rs.45,000.00 credited to A/c XX4521")
        assertThat(amount).isEqualTo(45000.00)
    }

    @Test
    fun `extracts amount with INR suffix`() {
        val amount = SmsFieldExtractor.extractAmount("1234.50 INR debited from your account")
        assertThat(amount).isEqualTo(1234.50)
    }

    @Test
    fun `extracts account last 4 digits`() {
        val acc = SmsFieldExtractor.extractAccountLast4("debited from A/c XX4521 on 01-07-26")
        assertThat(acc).isEqualTo("4521")
    }

    @Test
    fun `extracts card last 4 digits`() {
        val card = SmsFieldExtractor.extractCardLast4("spent on your SBI Credit Card XX7712 at AMAZON")
        assertThat(card).isEqualTo("7712")
    }

    @Test
    fun `extracts available balance`() {
        val bal = SmsFieldExtractor.extractAvailableBalance("Avl Bal Rs.67690.00")
        assertThat(bal).isEqualTo(67690.00)
    }

    @Test
    fun `detects UPI payment mode`() {
        val mode = SmsFieldExtractor.extractPaymentMode("Rs.850.00 debited to SWIGGY via UPI")
        assertThat(mode).isEqualTo(com.wealthwise.app.domain.model.PaymentMode.UPI)
    }

    @Test
    fun `does not extract amount from message with no currency markers`() {
        val amount = SmsFieldExtractor.extractAmount("Your OTP is 482913. Do not share.")
        assertThat(amount).isNull()
    }
}
