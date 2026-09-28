package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Divider
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Data & Theme ---
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun ColumnScope.RecentHistorySection(
    items: List<HistoryItem>,
    colors: AksoroColors,
    appFont: FontFamily
) {
    if (items.isEmpty()) return

    Divider(
        color = colors.textTertiary,
        thickness = 1.dp,
        modifier = Modifier.padding(bottom = 16.dp).background(Color.Transparent)
    )

    Text(
        text = "Riwayat Terbaru",
        fontFamily = appFont,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = colors.textPrimary,
        modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
    )

    items.forEach { item ->
        HistoryCard(item = item, colors = colors, appFont = appFont)
    }
    Spacer(modifier = Modifier.height(32.dp))
}