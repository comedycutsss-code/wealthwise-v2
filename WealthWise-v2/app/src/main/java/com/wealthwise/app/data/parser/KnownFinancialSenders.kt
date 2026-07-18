package com.wealthwise.app.data.parser

import java.util.Locale

/**
 * Indian bank/NBFC SMS sender IDs are typically 6-character DLT-registered codes like
 * "VM-HDFCBK", "AD-ICICIB", "JD-SBIINB". Matching against known institution fragments
 * (rather than requiring an exact full sender match) keeps this resilient to the many
 * telecom-operator prefixes (VM-, AD-, JD-, TX-, BZ- etc.) attached to the same sender.
 */
object KnownFinancialSenders {

    private val bankFragments = listOf(
        "HDFCBK", "ICICIB", "SBIINB", "SBICRD", "AXISBK", "KOTAKB", "PNBSMS", "BOIIND",
        "CANBNK", "UNIONB", "IDFCFB", "YESBNK", "INDUSB", "RBLBNK", "FEDBNK", "IDBI",
        "CENTBK", "UCOBNK", "BOBIBK"
    )
    private val upiFragments = listOf("PAYTM", "PHONPE", "GPAY", "BHIM", "AMAZONPAY", "MOBIKW")
    private val brokerFragments = listOf("ZERODHA", "GROWWS", "UPSTOX", "ANGELO", "ICICIDR", "HDFCSKY", "MOSL")
    private val mfFragments = listOf("CAMSMF", "KFINTE", "ETMONY", "KUVERA", "PAYTMM")
    private val insuranceFragments = listOf("LICIND", "SBILIF", "HDFCLI", "ICICPR", "MAXLIF", "BAJAJA", "TATAIA", "STARHL", "NIVABP")
    private val loanFragments = listOf("BAJFIN", "MUTHOOT", "HDBFSL", "LNTFIN", "IIFL")

    private val allFragments = (bankFragments + upiFragments + brokerFragments + mfFragments + insuranceFragments + loanFragments)

    fun isKnownFinancialSender(senderId: String): Boolean {
        val normalized = senderId.uppercase(Locale.ROOT)
        return allFragments.any { normalized.contains(it) }
    }

    /**
     * Real-world sender IDs vary a lot by telecom circle. As a safety net for institutions
     * not in the curated list above, callers should still run [TransactionClassifier.isLikelyFinancial]
     * on the body — sender matching alone should never be the sole gate.
     */
}
