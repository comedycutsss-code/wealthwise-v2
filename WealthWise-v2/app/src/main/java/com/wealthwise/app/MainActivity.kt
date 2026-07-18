package com.wealthwise.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.wealthwise.app.presentation.common.navigation.WealthWiseNavGraph
import com.wealthwise.app.presentation.common.theme.WealthWiseTheme
import com.wealthwise.app.worker.SmsScanWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enqueueInitialScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WealthWiseTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var hasPermission by remember { mutableStateOf(hasSmsPermission()) }

                    if (hasPermission) {
                        WealthWiseNavGraph()
                    } else {
                        PermissionGate(
                            onRequestPermission = {
                                requestSmsPermission.launch(Manifest.permission.READ_SMS)
                                hasPermission = hasSmsPermission()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

    private fun enqueueInitialScan() {
        val request = OneTimeWorkRequestBuilder<SmsScanWorker>().build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            SmsScanWorker.WORK_NAME,
            androidx.work.ExistingWorkPolicy.KEEP,
            request
        )
    }
}

@androidx.compose.runtime.Composable
private fun PermissionGate(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "WealthWise reads your bank SMS locally on this device to build your dashboard. Nothing is ever uploaded.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(onClick = onRequestPermission) {
                Text("Grant SMS access")
            }
        }
    }
}
