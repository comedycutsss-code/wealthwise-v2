package com.wealthwise.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wealthwise.app.data.local.dao.TransactionDao
import com.wealthwise.app.data.local.entity.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlySavings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val netWorthTrend: List<Float> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dao: TransactionDao
) : ViewModel() {

    private fun monthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val from = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val to = cal.timeInMillis - 1
        return from to to
    }

    val uiState = run {
        val (from, to) = monthRange()
        combine(
            dao.observeIncomeBetween(from, to),
            dao.observeExpenseBetween(from, to),
            dao.observeRecent(10)
        ) { income, expense, recent ->
            val savings = income - expense
            val rate = if (income > 0) (savings / income) * 100 else 0.0
            DashboardUiState(
                monthlyIncome = income,
                monthlyExpense = expense,
                monthlySavings = savings,
                savingsRate = rate,
                recentTransactions = recent,
                // Trend placeholder derived from recent transaction running balance;
                // a dedicated observeMonthlyNetWorth() query should replace this once
                // net-worth snapshots are tracked per month.
                netWorthTrend = recent.mapNotNull { it.availableBalance?.toFloat() }.reversed()
                    .ifEmpty { listOf(0f, 0f) },
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
    }
}
