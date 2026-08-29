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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.BillWithPerson
import com.example.billease.ui.components.StatusBadge
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatDate
import com.example.billease.util.formatMoney

@Suppress("LongMethod", "LongParameterList")
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToProductForm: () -> Unit,
    onNavigateToBillForm: () -> Unit,
    onNavigateToPersonForm: () -> Unit,
    onNavigateToBillDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val customerCount by viewModel.customerCount.collectAsState()
    val totalBills by viewModel.totalBills.collectAsState()
    val revenueThisMonth by viewModel.revenueThisMonth.collectAsState()
    val recentBills by viewModel.recentBills.collectAsState()
    val businessNameInitial by viewModel.businessNameInitial.collectAsState()
    val homeSearchQuery by viewModel.homeSearchQuery.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val backgroundBrush =
        Brush.verticalGradient(
            colors = listOf(colorScheme.surfaceVariant, colorScheme.surface),
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
                            .background(colorScheme.surfaceVariant)
                            .border(1.dp, colorScheme.outlineVariant, CircleShape)
                            .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = businessNameInitial,
                        color = colorScheme.onSurfaceVariant,
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
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colorScheme.onSurfaceVariant,
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
                            unfocusedBorderColor = colorScheme.outlineVariant,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedContainerColor = colorScheme.surface,
                            cursorColor = colorScheme.primary,
                        ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                )
            }

            // Hero Section
            HeroCard(
                revenueThisMonth = revenueThisMonth,
                totalBills = totalBills,
                customerCount = customerCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )

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
                    color = colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "See all",
                    color = colorScheme.primary,
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
                            Text("No recent bills", color = colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(recentBills) { billWithPerson ->
                        RecentBillCard(
                            billWithPerson = billWithPerson,
                            onClick = { onNavigateToBillDetail(billWithPerson.bill.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddButton(
    label: String,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = label,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun HeroCard(
    revenueThisMonth: Double,
    totalBills: Int,
    customerCount: Int,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(20.dp),
    ) {
        Text(
            text = "TOTAL SALES · THIS MONTH",
            color = colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = formatMoney(revenueThisMonth, LocalCurrencyCode.current),
            color = colorScheme.onSurface,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false,
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            HeroStat(label = "SALES", value = totalBills.toString())
            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(colorScheme.onSurface.copy(alpha = 0.15f)),
            )
            HeroStat(label = "CUSTOMERS", value = customerCount.toString())
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RecentBillCard(
    billWithPerson: BillWithPerson,
    onClick: () -> Unit,
) {
    val dateString = formatDate(billWithPerson.bill.billDate)
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .border(1.dp, colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecentBillIcon()

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = billWithPerson.person.name,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${billWithPerson.bill.billNumber} • $dateString",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatMoney(billWithPerson.bill.grandTotal, LocalCurrencyCode.current),
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            StatusBadge(billWithPerson.bill.paymentStatus)
        }
    }
}

@Composable
private fun RecentBillIcon() {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}
