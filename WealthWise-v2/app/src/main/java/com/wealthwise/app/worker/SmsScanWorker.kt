package com.wealthwise.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.wealthwise.app.data.repository.SmsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SmsScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val smsRepository: SmsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val summary = smsRepository.scanAllSms()
            Result.success(
                workDataOf(
                    KEY_SCANNED to summary.scanned,
                    KEY_FINANCIAL to summary.financialFound,
                    KEY_INSERTED to summary.transactionsInserted
                )
            )
        } catch (e: SecurityException) {
            // READ_SMS permission missing or revoked mid-scan.
            Result.failure(workDataOf(KEY_ERROR to "permission_denied"))
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "wealthwise_initial_sms_scan"
        const val KEY_SCANNED = "scanned"
        const val KEY_FINANCIAL = "financial"
        const val KEY_INSERTED = "inserted"
        const val KEY_ERROR = "error"
    }
}
