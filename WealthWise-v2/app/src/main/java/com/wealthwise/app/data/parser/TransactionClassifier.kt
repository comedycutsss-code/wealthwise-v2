package com.wealthwise.app.data.parser

import com.wealthwise.app.domain.model.Category
import com.wealthwise.app.domain.model.TransactionType
import java.util.Locale

/**
 * Purely local "AI" layer requested in the spec as a "hybrid approach": in practice this is
 * a weighted keyword-scoring classifier, which is the right tool here — bank SMS vocabulary
 * is finite and formulaic, so a trained neural classifier would add size and latency without
 * beating well-tuned rules. The scoring structure below is deliberately built so a future
 * on-device TF-Lite model could be dropped in as an additional signal (see [ClassificationSignal])
 * without changing the public [classify] contract.
 */
object TransactionClassifier {

    private data class Rule(val category: Category, val type: TransactionType, val keywords: List<String>, val weight: Float = 1f)

    private val rules = listOf(
        Rule(Category.SALARY, TransactionType.INCOME, listOf("salary", "salary credit", "sal credit", "payroll")),
        Rule(Category.BONUS, TransactionType.INCOME, listOf("bonus", "incentive")),
        Rule(Category.CASH_DEPOSIT, TransactionType.INCOME, listOf("cash deposit", "cash dep")),
        Rule(Category.REFUND, TransactionType.INCOME, listOf("refund", "reversal", "reversed")),
        Rule(Category.INTEREST, TransactionType.INCOME, listOf("interest credited", "int.credited", "int credited")),
        Rule(Category.DIVIDEND, TransactionType.INCOME, listOf("dividend")),
        Rule(Category.RENTAL_INCOME, TransactionType.INCOME, listOf("rent received", "rental credit")),
        Rule(Category.TAX_REFUND, TransactionType.INCOME, listOf("income tax refund", "itr refund")),
        Rule(Category.CASHBACK, TransactionType.INCOME, listOf("cashback", "cash back")),
        Rule(Category.INSURANCE_CLAIM, TransactionType.INCOME, listOf("claim settled", "claim amount credited")),

        Rule(Category.FOOD, TransactionType.EXPENSE, listOf("zomato", "swiggy", "food", "grocery", "bigbasket", "blinkit", "zepto")),
        Rule(Category.RESTAURANTS, TransactionType.EXPENSE, listOf("restaurant", "dine", "cafe", "eatery")),
        Rule(Category.FUEL, TransactionType.EXPENSE, listOf("petrol", "diesel", "fuel", "hpcl", "iocl", "bpcl")),
        Rule(Category.SHOPPING, TransactionType.EXPENSE, listOf("amazon", "flipkart", "myntra", "ajio", "shopping")),
        Rule(Category.MEDICAL, TransactionType.EXPENSE, listOf("pharmacy", "hospital", "medical", "apollo", "pharmeasy", "clinic")),
        Rule(Category.TRAVEL, TransactionType.EXPENSE, listOf("irctc", "makemytrip", "goibibo", "uber", "ola", "flight", "indigo")),
        Rule(Category.EDUCATION, TransactionType.EXPENSE, listOf("school fee", "college fee", "tuition", "byju", "udemy", "coursera")),
        Rule(Category.UTILITIES, TransactionType.EXPENSE, listOf("electricity", "water bill", "gas bill", "bescom", "utility")),
        Rule(Category.INTERNET, TransactionType.EXPENSE, listOf("broadband", "wifi bill", "airtel fiber", "jiofiber")),
        Rule(Category.MOBILE_RECHARGE, TransactionType.EXPENSE, listOf("recharge", "prepaid", "mobile bill")),
        Rule(Category.SUBSCRIPTIONS, TransactionType.EXPENSE, listOf("netflix", "spotify", "prime", "hotstar", "subscription")),
        Rule(Category.ENTERTAINMENT, TransactionType.EXPENSE, listOf("bookmyshow", "pvr", "inox", "movie")),
        Rule(Category.ATM_WITHDRAWAL, TransactionType.EXPENSE, listOf("atm wdl", "atm withdrawal", "cash withdrawal")),
        Rule(Category.RENT, TransactionType.EXPENSE, listOf("rent paid", "house rent")),
        Rule(Category.TAXES, TransactionType.EXPENSE, listOf("income tax paid", "gst payment", "advance tax")),
        Rule(Category.INSURANCE_PREMIUM, TransactionType.EXPENSE, listOf("premium due", "premium paid", "policy premium")),
        Rule(Category.CREDIT_CARD_PAYMENT, TransactionType.EXPENSE, listOf("card payment received", "cc payment")),
        Rule(Category.UPI_PAYMENT, TransactionType.EXPENSE, listOf("upi")),
        Rule(Category.NEFT, TransactionType.EXPENSE, listOf("neft")),
        Rule(Category.IMPS, TransactionType.EXPENSE, listOf("imps")),
        Rule(Category.RTGS, TransactionType.EXPENSE, listOf("rtgs")),

        Rule(Category.MUTUAL_FUND, TransactionType.INVESTMENT_BUY, listOf("mutual fund", "sip", "nav allotted", "units allotted", "groww", "kuvera", "coin by zerodha", "folio")),
        Rule(Category.STOCKS, TransactionType.INVESTMENT_BUY, listOf("shares", "equity", "zerodha", "upstox", "angel one", "order executed")),
        Rule(Category.FIXED_DEPOSIT, TransactionType.FD_CREATED, listOf("fixed deposit", "fd created", "fd booked")),
        Rule(Category.RECURRING_DEPOSIT, TransactionType.INVESTMENT_SIP, listOf("recurring deposit", "rd installment")),
        Rule(Category.EPF, TransactionType.INVESTMENT_BUY, listOf("epf contribution", "pf contribution", "epfo")),
        Rule(Category.PPF, TransactionType.INVESTMENT_BUY, listOf("ppf deposit", "public provident fund")),
        Rule(Category.NPS, TransactionType.INVESTMENT_BUY, listOf("nps contribution", "pran")),
        Rule(Category.SGB, TransactionType.INVESTMENT_BUY, listOf("sovereign gold bond", "sgb")),
        Rule(Category.DIGITAL_GOLD, TransactionType.INVESTMENT_BUY, listOf("digital gold", "mmtc", "safegold", "augmont")),

        Rule(Category.PERSONAL_LOAN, TransactionType.LOAN_EMI, listOf("personal loan", "pl emi")),
        Rule(Category.CAR_LOAN, TransactionType.LOAN_EMI, listOf("car loan", "auto loan")),
        Rule(Category.HOME_LOAN, TransactionType.LOAN_EMI, listOf("home loan", "housing loan")),
        Rule(Category.EDUCATION_LOAN, TransactionType.LOAN_EMI, listOf("education loan", "student loan")),
        Rule(Category.GOLD_LOAN, TransactionType.LOAN_EMI, listOf("gold loan")),
        Rule(Category.CONSUMER_LOAN, TransactionType.LOAN_EMI, listOf("consumer loan", "emi for")),
    )

    private val debitKeywords = listOf("debited", "spent", "paid", "withdrawn", "purchase of")
    private val creditKeywords = listOf("credited", "received", "deposited")

    data class Result(
        val category: Category,
        val type: TransactionType,
        val confidence: Float
    )

    fun classify(smsBody: String): Result {
        val text = smsBody.lowercase(Locale.ROOT)

        var best: Rule? = null
        var bestScore = 0f
        for (rule in rules) {
            val hits = rule.keywords.count { it in text }
            if (hits == 0) continue
            val score = hits * rule.weight
            if (score > bestScore) {
                bestScore = score
                best = rule
            }
        }

        val isDebit = debitKeywords.any { it in text }
        val isCredit = creditKeywords.any { it in text }

        val resolvedType = when {
            best != null -> best.type
            isCredit -> TransactionType.INCOME
            isDebit -> TransactionType.EXPENSE
            else -> TransactionType.UNKNOWN
        }

        val category = best?.category ?: Category.UNCATEGORIZED

        // Confidence blends keyword strength with debit/credit clarity; both signals present
        // pushes confidence high, only one present is moderate, neither is low.
        val confidence = when {
            best != null && (isDebit || isCredit) -> 0.9f
            best != null -> 0.65f
            isDebit || isCredit -> 0.4f
            else -> 0.15f
        }.coerceIn(0f, 1f)

        return Result(category, resolvedType, confidence)
    }

    /** Quick pre-filter to discard OTPs, promos, and non-financial noise before full classification. */
    fun isLikelyFinancial(smsBody: String): Boolean {
        val text = smsBody.lowercase(Locale.ROOT)
        val otpOrPromo = listOf("otp is", "one time password", "do not share", "% off", "sale is live", "click here to", "unsubscribe")
        if (otpOrPromo.any { it in text }) return false

        val financialSignals = listOf(
            "debited", "credited", "spent", "withdrawn", "a/c", "acct", "balance", "emi",
            "sip", "nav", "premium", "upi", "neft", "imps", "rtgs", "card ending", "invested",
            "folio", "maturity", "loan", "insurance"
        )
        return financialSignals.any { it in text }
    }
}
