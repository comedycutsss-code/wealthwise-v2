package com.wealthwise.app.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.wealthwise.app.presentation.common.components.InkStrokeLineChart
import com.wealthwise.app.presentation.common.components.MetricCard
import com.wealthwise.app.presentation.common.theme.LedgerJade
import com.wealthwise.app.presentation.common.theme.LedgerRust

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenTransaction: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WealthWise", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StaggeredEntry(visible = contentVisible, delayMillis = 0) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("This month's cash flow", style = MaterialTheme.typography.titleMedium)
                        InkStrokeLineChart(
                            values = state.netWorthTrend,
                            lineColor = if (state.monthlySavings >= 0) LedgerJade else LedgerRust
                        )
                    }
                }
            }

            item {
                StaggeredEntry(visible = contentVisible, delayMillis = 80) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        MetricCard(
                            label = "Income",
                            value = formatCurrency(state.monthlyIncome),
                            accent = LedgerJade,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Expenses",
                            value = formatCurrency(state.monthlyExpense),
                            accent = LedgerRust,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                StaggeredEntry(visible = contentVisible, delayMillis = 140) {
                    MetricCard(
                        label = "Savings this month",
                        value = formatCurrency(state.monthlySavings),
                        subtitle = "Savings rate: ${"%.1f".format(state.savingsRate)}%",
                        accent = if (state.monthlySavings >= 0) LedgerJade else LedgerRust
                    )
                }
            }

            item {
                Text(
                    "Recent transactions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(state.recentTransactions) { index, txn ->
                StaggeredEntry(visible = contentVisible, delayMillis = 200 + index * 30) {
                    TransactionRow(txn = txn, onClick = { onOpenTransaction(txn.id) })
                }
            }

            if (state.recentTransactions.isEmpty() && !state.isLoading) {
                item {
                    Text(
                        "No transactions yet. Run the first SMS scan from Settings to populate your dashboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                    )
                }
            }
        }
    }
}

/** Staggered fade + rise entrance used for every dashboard block, tuned to feel like items settling onto a page rather than popping in. */
@Composable
private fun StaggeredEntry(visible: Boolean, delayMillis: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 420, delayMillis = delayMillis)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 420, delayMillis = delayMillis),
                initialOffsetY = { it / 6 }
            )
    ) {
        content()
    }
}

private fun formatCurrency(amount: Double): String {
    val sign = if (amount < 0) "-" else ""
    return "$sign₹${"%,.0f".format(kotlin.math.abs(amount))}"
}
