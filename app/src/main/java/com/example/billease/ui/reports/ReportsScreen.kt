package com.example.billease.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.billease.ui.components.ProfileIconButton
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatMoney

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
        }
    }
}

@Composable
private fun StatsRow(
    cards: List<Pair<String, String>>,
) {
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
