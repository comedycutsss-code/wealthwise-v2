package com.wealthwise.app.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wealthwise.app.data.local.entity.TransactionEntity
import com.wealthwise.app.domain.model.TransactionType
import com.wealthwise.app.presentation.common.theme.LedgerJade
import com.wealthwise.app.presentation.common.theme.LedgerRust
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionRow(txn: TransactionEntity, onClick: () -> Unit) {
    val isIncome = txn.type == TransactionType.INCOME
    val amountColor = if (isIncome) LedgerJade else LedgerRust
    val sign = if (isIncome) "+" else "−"
    val dateFmt = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                txn.merchant ?: txn.category.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "${txn.category.displayName} · ${dateFmt.format(Date(txn.date))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Text(
            "$sign₹${"%,.0f".format(txn.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
    }
}
