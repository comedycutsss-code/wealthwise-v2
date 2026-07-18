package com.wealthwise.app.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.wealthwise.app.worker.SmsScanWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    val workInfos by workManager.getWorkInfosForUniqueWorkLiveData(SmsScanWorker.WORK_NAME)
        .observeAsState(emptyList())
    val latestWork = workInfos.firstOrNull()
    val isScanning = latestWork?.state == WorkInfo.State.RUNNING || latestWork?.state == WorkInfo.State.ENQUEUED

    val hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
        PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                if (hasSmsPermission) "SMS permission granted." else "SMS permission not granted — reopen the app and accept the prompt to enable scanning.",
                style = MaterialTheme.typography.bodyMedium
            )

            when (latestWork?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    val scanned = latestWork.outputData.getInt(SmsScanWorker.KEY_SCANNED, 0)
                    val financial = latestWork.outputData.getInt(SmsScanWorker.KEY_FINANCIAL, 0)
                    val inserted = latestWork.outputData.getInt(SmsScanWorker.KEY_INSERTED, 0)
                    Text(
                        "Last scan: checked $scanned messages, found $financial financial, added $inserted new transactions.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                WorkInfo.State.FAILED -> Text(
                    "Last scan failed. Make sure SMS permission is granted, then try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                else -> {}
            }

            Button(
                onClick = { com.wealthwise.app.worker.enqueueSmsScan(context) },
                enabled = hasSmsPermission && !isScanning,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).size(16.dp))
                    Text("Scanning…")
                } else {
                    Text("Scan SMS now")
                }
            }
        }
    }
}
