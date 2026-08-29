package com.example.billease.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.MonthlyRevenueRow
import com.example.billease.data.ProductTotalRow
import com.example.billease.ui.components.ProfileIconButton
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatMoney
import com.example.billease.util.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val totalBillCount by viewModel.totalBillCount.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val totalOutstanding by viewModel.totalOutstanding.collectAsState()
    val billsThisMonth by viewModel.billsThisMonth.collectAsState()
    val revenueThisMonth by viewModel.revenueThisMonth.collectAsState()
    val outstandingThisMonth by viewModel.outstandingThisMonth.collectAsState()
    val monthlyRevenue by viewModel.monthlyRevenue.collectAsState()
    val productTotals by viewModel.productTotals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                actions = { ProfileIconButton(onClick = onNavigateToSettings) },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("This Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            StatsRow(
                cards =
                    listOf(
                        "Bills" to billsThisMonth.toString(),
                        "Collected" to formatMoney(revenueThisMonth, LocalCurrencyCode.current),
                        "Outstanding" to formatMoney(outstandingThisMonth, LocalCurrencyCode.current),
                    ),
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text("All Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            StatsRow(
                cards =
                    listOf(
                        "Total Bills" to totalBillCount.toString(),
                        "Collected" to formatMoney(totalRevenue, LocalCurrencyCode.current),
                        "Outstanding" to formatMoney(totalOutstanding, LocalCurrencyCode.current),
                    ),
            )

            if (monthlyRevenue.isNotEmpty() || productTotals.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
            }

            MonthlyBreakdownSection(rows = monthlyRevenue)
            ProductTotalsSection(rows = productTotals)
        }
    }
}

@Composable
private fun MonthlyBreakdownSection(rows: List<MonthlyRevenueRow>) {
    if (rows.isEmpty()) return
    Text("Monthly Collected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    rows.forEach { row ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(monthLabel(row.month), modifier = Modifier.weight(1f))
            Text(formatMoney(row.revenue, LocalCurrencyCode.current), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProductTotalsSection(rows: List<ProductTotalRow>) {
    if (rows.isEmpty()) return
    Text("Per-Product Totals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    rows.forEach { row ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(row.productName, modifier = Modifier.weight(1f))
            Text(formatQuantity(row.quantity), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(formatMoney(row.revenue, LocalCurrencyCode.current), fontWeight = FontWeight.Bold)
        }
    }
}

private val MONTH_ABBR = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun monthLabel(yyyyMm: String): String {
    val parts = yyyyMm.split("-")
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return yyyyMm
    return if (month in 1..MONTH_ABBR.size) {
        "${MONTH_ABBR[month - 1]} ${parts[0]}"
    } else {
        yyyyMm
    }
}

@Composable
private fun StatsRow(cards: List<Pair<String, String>>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.forEach { (label, value) ->
            StatCard(label = label, value = value, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
