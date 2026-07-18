package com.wealthwise.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        for (msg in messages) {
            val sender = msg.originatingAddress ?: continue
            val body = msg.messageBody ?: continue
            val timestamp = msg.timestampMillis

            val request = OneTimeWorkRequestBuilder<SmsProcessIncomingWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(SmsProcessIncomingWorker.KEY_SENDER, sender)
                        .putString(SmsProcessIncomingWorker.KEY_BODY, body)
                        .putLong(SmsProcessIncomingWorker.KEY_TIMESTAMP, timestamp)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
