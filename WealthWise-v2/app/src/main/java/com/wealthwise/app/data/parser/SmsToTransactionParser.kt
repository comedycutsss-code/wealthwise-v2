package com.wealthwise.app.data.parser

import com.wealthwise.app.data.local.entity.TransactionEntity
import com.wealthwise.app.domain.model.TransactionType
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class RawSms(val sender: String, val body: String, val timestampMillis: Long)

@Singleton
class SmsToTransactionParser @Inject constructor() {

    fun hash(sms: RawSms): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("${sms.sender}|${sms.body}|${sms.timestampMillis}".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Returns a fully-formed [TransactionEntity] draft (rawSmsId must be set by the caller
     * after the raw SMS row is inserted), or null if the message doesn't look financial or
     * couldn't yield a usable amount.
     */
    fun parse(sms: RawSms): ParsedResult? {
        val looksFinancial = KnownFinancialSenders.isKnownFinancialSender(sms.sender) ||
            TransactionClassifier.isLikelyFinancial(sms.body)
        if (!looksFinancial) return ParsedResult(isFinancial = false, transaction = null)
        if (!TransactionClassifier.isLikelyFinancial(sms.body)) {
            return ParsedResult(isFinancial = false, transaction = null)
        }

        val amount = SmsFieldExtractor.extractAmount(sms.body) ?: return ParsedResult(isFinancial = true, transaction = null)
        val classification = TransactionClassifier.classify(sms.body)
        val paymentMode = SmsFieldExtractor.extractPaymentMode(sms.body)

        val entity = TransactionEntity(
            rawSmsId = null,
            date = sms.timestampMillis,
            amount = amount,
            type = classification.type,
            category = classification.category,
            paymentMode = paymentMode,
            bank = inferBankName(sms.sender),
            merchant = SmsFieldExtractor.extractMerchant(sms.body),
            sender = sms.sender,
            accountLast4 = SmsFieldExtractor.extractAccountLast4(sms.body),
            cardLast4 = SmsFieldExtractor.extractCardLast4(sms.body),
            upiId = SmsFieldExtractor.extractUpiId(sms.body),
            transactionId = SmsFieldExtractor.extractReferenceNumber(sms.body),
            referenceNumber = SmsFieldExtractor.extractReferenceNumber(sms.body),
            availableBalance = SmsFieldExtractor.extractAvailableBalance(sms.body),
            description = sms.body.take(200),
            confidenceScore = classification.confidence,
            createdAtMillis = System.currentTimeMillis()
        )
        return ParsedResult(isFinancial = true, transaction = entity)
    }

    private fun inferBankName(sender: String): String {
        val upper = sender.uppercase()
        return when {
            "HDFC" in upper -> "HDFC Bank"
            "ICICI" in upper -> "ICICI Bank"
            "SBI" in upper -> "State Bank of India"
            "AXIS" in upper -> "Axis Bank"
            "KOTAK" in upper -> "Kotak Mahindra Bank"
            "PNB" in upper -> "Punjab National Bank"
            else -> sender
        }
    }

    data class ParsedResult(val isFinancial: Boolean, val transaction: TransactionEntity?)
}
