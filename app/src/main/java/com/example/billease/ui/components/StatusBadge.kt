package com.example.billease.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.billease.data.BillStatus

@Composable
fun StatusBadge(status: BillStatus) {
    val (statusColor, statusBg) =
        if (isSystemInDarkTheme()) {
            darkColors(status)
        } else {
            lightColors(status)
        }
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(statusBg)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = status.name,
            color = statusColor,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Suppress("MagicNumber")
private fun lightColors(status: BillStatus): Pair<Color, Color> =
    when (status) {
        BillStatus.PAID -> Color(0xFF15803D) to Color(0xFFDCFCE7)
        BillStatus.OVERDUE -> Color(0xFFB91C1C) to Color(0xFFFEE2E2)
        BillStatus.PENDING -> Color(0xFFB45309) to Color(0xFFFEF3C7)
    }

@Suppress("MagicNumber")
private fun darkColors(status: BillStatus): Pair<Color, Color> =
    when (status) {
        BillStatus.PAID -> Color(0xFF22C55E) to Color(0xFF14532D)
        BillStatus.OVERDUE -> Color(0xFFEF4444) to Color(0xFF7F1D1D)
        BillStatus.PENDING -> Color(0xFFF59E0B) to Color(0xFF78350F)
    }
