package com.example.billease.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.BillWithPerson
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("FunctionNaming", "MagicNumber", "LongMethod", "LongParameterList")
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToProductForm: () -> Unit,
    onNavigateToBillForm: () -> Unit,
    onNavigateToPersonForm: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val customerCount by viewModel.customerCount.collectAsState()
    val revenueThisMonth by viewModel.revenueThisMonth.collectAsState()
    val recentBills by viewModel.recentBills.collectAsState()
    val businessNameInitial by viewModel.businessNameInitial.collectAsState()
    val homeSearchQuery by viewModel.homeSearchQuery.collectAsState()

    // Luminous Ledger Gradient Background
    val backgroundBrush =
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF020617)),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundBrush),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            // Header
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = businessNameInitial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }

                OutlinedTextField(
                    value = homeSearchQuery,
                    onValueChange = viewModel::onHomeSearchChange,
                    placeholder = {
                        Text(
                            text = "Search by name or invoice no.",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(52.dp),
                    singleLine = true,
                    colors =
                        androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedContainerColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF3B82F6),
                        ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                )
            }

            // Hero Section
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Total Sales - This Month",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatMoney(revenueThisMonth, LocalCurrencyCode.current),
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$customerCount customers",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Add Buttons: Product, Bill, Customer
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                QuickAddButton(
                    label = "Add Product",
                    onClick = onNavigateToProductForm,
                )
                QuickAddButton(
                    label = "Add Bill",
                    onClick = onNavigateToBillForm,
                )
                QuickAddButton(
                    label = "Add Customer",
                    onClick = onNavigateToPersonForm,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Bills Header
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent Bills",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "See all",
                    color = Color(0xFF3B82F6),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToBills() },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Bills List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
            ) {
                if (recentBills.isEmpty()) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No recent bills", color = Color(0xFF64748B))
                        }
                    }
                } else {
                    items(recentBills) { billWithPerson ->
                        RecentBillCard(billWithPerson)
                    }
                }
            }
        }
    }
}

@Suppress("FunctionNaming", "MagicNumber")
@Composable
private fun QuickAddButton(
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = label,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Suppress("FunctionNaming", "MagicNumber", "LongMethod")
@Composable
private fun RecentBillCard(billWithPerson: BillWithPerson) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = dateFormat.format(java.util.Date(billWithPerson.bill.createdAt))

    val statusText = billWithPerson.bill.paymentStatus
    val (statusColor, statusBg) =
        when (statusText.uppercase()) {
            "PAID" -> Color(0xFF22C55E) to Color(0xFF14532D)
            "OVERDUE" -> Color(0xFFEF4444) to Color(0xFF7F1D1D)
            else -> Color(0xFFF59E0B) to Color(0xFF78350F)
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = billWithPerson.person.name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${billWithPerson.bill.billNumber} • $dateString",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount & Status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatMoney(billWithPerson.bill.grandTotal, LocalCurrencyCode.current),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
