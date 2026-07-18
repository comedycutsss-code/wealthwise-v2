package com.wealthwise.app.data.parser

import com.wealthwise.app.domain.model.PaymentMode
import java.util.Locale

/**
 * Stateless regex-based field extraction. Every pattern here is intentionally permissive
 * (Indian bank SMS formats vary a lot bank-to-bank) and returns null rather than guessing
 * when it isn't confident — downstream scoring in [TransactionClassifier] treats missing
 * fields as reduced confidence, not hard failure.
 */
object SmsFieldExtractor {

    // Matches "Rs.1,234.50", "INR 1234", "₹ 1,234", "1234.00 INR"
    private val amountRegex = Regex(
        """(?:Rs\.?|INR|₹)\s?([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)|([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)\s?(?:Rs\.?|INR)""",
        RegexOption.IGNORE_CASE
    )

    private val accountLast4Regex = Regex(
        """(?:a/?c|account|acct)[\w\s]*?(?:no\.?|number)?[\s.:xX*]*(?:[xX*]{2,}|ending)?\s*([0-9]{4})\b""",
        RegexOption.IGNORE_CASE
    )

    private val cardLast4Regex = Regex(
        """card[\w\s]*?(?:ending|no\.?)?[\s.:xX*]*(?:[xX*]{2,})?\s*([0-9]{4})\b""",
        RegexOption.IGNORE_CASE
    )

    private val upiIdRegex = Regex("""[\w.\-]{2,}@[a-zA-Z]{2,}""")

    private val referenceRegex = Regex(
        """(?:ref(?:erence)?\s?(?:no\.?|number)?|txn\s?id|transaction\s?id|utr)[\s:.-]*([A-Za-z0-9]{6,20})""",
        RegexOption.IGNORE_CASE
    )

    private val balanceRegex = Regex(
        """(?:avl\.?\s?bal|available\s?balance|bal)[\s:.]*(?:Rs\.?|INR|₹)?\s?([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    fun extractAmount(text: String): Double? {
        val match = amountRegex.find(text) ?: return null
        val raw = match.groupValues[1].ifBlank { match.groupValues[2] }
        return raw.replace(",", "").toDoubleOrNull()
    }

    fun extractAccountLast4(text: String): String? = accountLast4Regex.find(text)?.groupValues?.get(1)

    fun extractCardLast4(text: String): String? = cardLast4Regex.find(text)?.groupValues?.get(1)

    fun extractUpiId(text: String): String? {
        val match = upiIdRegex.find(text) ?: return null
        // Filter out email-shaped noise that isn't a UPI handle (best-effort heuristic:
        // UPI handles rarely use common email domains).
        val commonEmailDomains = setOf("gmail.com", "yahoo.com", "outlook.com", "hotmail.com")
        val domain = match.value.substringAfter("@").lowercase(Locale.ROOT)
        return if (domain in commonEmailDomains) null else match.value
    }

    fun extractReferenceNumber(text: String): String? = referenceRegex.find(text)?.groupValues?.get(1)

    fun extractAvailableBalance(text: String): Double? {
        val match = balanceRegex.find(text) ?: return null
        return match.groupValues[1].replace(",", "").toDoubleOrNull()
    }

    fun extractPaymentMode(text: String): PaymentMode {
        val t = text.lowercase(Locale.ROOT)
        return when {
            "upi" in t -> PaymentMode.UPI
            "neft" in t -> PaymentMode.NEFT
            "imps" in t -> PaymentMode.IMPS
            "rtgs" in t -> PaymentMode.RTGS
            "atm" in t -> PaymentMode.CASH
            "credit card" in t || "debit card" in t || "card ending" in t -> PaymentMode.CARD
            "auto debit" in t || "auto-debit" in t || "standing instruction" in t -> PaymentMode.AUTO_DEBIT
            "net banking" in t || "netbanking" in t -> PaymentMode.NET_BANKING
            "wallet" in t -> PaymentMode.WALLET
            "cheque" in t || "chq" in t -> PaymentMode.CHEQUE
            else -> PaymentMode.UNKNOWN
        }
    }

    /** Best-effort merchant extraction from common "to <merchant>" / "at <merchant>" patterns. */
    fun extractMerchant(text: String): String? {
        val patterns = listOf(
            Regex("""(?:paid to|to|at|towards)\s+([A-Z][A-Za-z0-9&.\-' ]{2,30})(?:\s+on|\s+via|\.|,|$)"""),
            Regex("""spent\s+(?:at|on)\s+([A-Za-z0-9&.\-' ]{2,30})""", RegexOption.IGNORE_CASE)
        )
        for (p in patterns) {
            p.find(text)?.let { return it.groupValues[1].trim() }
        }
        return null
    }
}
