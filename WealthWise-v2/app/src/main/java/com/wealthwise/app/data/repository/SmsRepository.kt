package com.wealthwise.app.data.repository

import android.content.Context
import android.provider.Telephony
import com.wealthwise.app.data.local.WealthWiseDatabase
import com.wealthwise.app.data.local.entity.RawSmsEntity
import com.wealthwise.app.data.parser.RawSms
import com.wealthwise.app.data.parser.SmsToTransactionParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WealthWiseDatabase,
    private val parser: SmsToTransactionParser
) {

    /**
     * Reads every SMS from the device inbox via ContentResolver (no network call — this is
     * a local content provider query) and inserts new financial transactions. Already-seen
     * messages (by hash) are skipped, which is what makes incremental re-scans on incoming
     * SMS cheap even with 50k+ historical messages.
     */
    suspend fun scanAllSms(batchSize: Int = 500): ScanSummary = withContext(Dispatchers.IO) {
        val knownHashes = database.transactionDao().getAllKnownHashes().toHashSet()
        var scanned = 0
        var financial = 0
        var inserted = 0

        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
            null, null,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use { c ->
            val addressIdx = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = c.getColumnIndexOrThrow(Telephony.Sms.DATE)

            val buffer = mutableListOf<RawSms>()
            while (c.moveToNext()) {
                val sender = c.getString(addressIdx) ?: continue
                val body = c.getString(bodyIdx) ?: continue
                val date = c.getLong(dateIdx)
                buffer.add(RawSms(sender, body, date))
                scanned++

                if (buffer.size >= batchSize) {
                    val result = processBatch(buffer, knownHashes)
                    financial += result.first
                    inserted += result.second
                    buffer.clear()
                }
            }
            if (buffer.isNotEmpty()) {
                val result = processBatch(buffer, knownHashes)
                financial += result.first
                inserted += result.second
            }
        }

        ScanSummary(scanned = scanned, financialFound = financial, transactionsInserted = inserted)
    }

    /** Processes a single freshly-arrived SMS (from the broadcast receiver). */
    suspend fun processIncoming(sms: RawSms): Boolean = withContext(Dispatchers.IO) {
        val hash = parser.hash(sms)
        val known = database.transactionDao().getAllKnownHashes().toHashSet()
        if (hash in known) return@withContext false
        processBatch(listOf(sms), known)
        true
    }

    /** Returns Pair(financialCount, insertedCount). */
    private suspend fun processBatch(batch: List<RawSms>, knownHashes: MutableSet<String>): Pair<Int, Int> {
        var financialCount = 0
        var insertedCount = 0
        val dao = database.transactionDao()

        for (sms in batch) {
            val hash = parser.hash(sms)
            if (hash in knownHashes) continue
            knownHashes.add(hash)

            val parsed = parser.parse(sms)
            val rawSmsId = dao.insertRawSms(
                RawSmsEntity(
                    sender = sms.sender,
                    body = sms.body,
                    timestampMillis = sms.timestampMillis,
                    messageHash = hash,
                    wasFinancial = parsed?.isFinancial == true,
                    processedAtMillis = System.currentTimeMillis()
                )
            )
            if (parsed?.isFinancial == true) financialCount++
            val txn = parsed?.transaction ?: continue
            dao.insertTransaction(txn.copy(rawSmsId = rawSmsId))
            insertedCount++
        }
        return financialCount to insertedCount
    }

    data class ScanSummary(val scanned: Int, val financialFound: Int, val transactionsInserted: Int)
}
