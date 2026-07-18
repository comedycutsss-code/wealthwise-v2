package com.wealthwise.app.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/** Enqueues a full SMS re-scan, replacing any currently queued/running scan. */
fun enqueueSmsScan(context: Context) {
    val request = OneTimeWorkRequestBuilder<SmsScanWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        SmsScanWorker.WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        request
    )
}
