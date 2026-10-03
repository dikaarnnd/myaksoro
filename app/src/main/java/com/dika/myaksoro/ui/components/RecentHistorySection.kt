package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
    appFont: FontFamily,
) {
    if (items.isEmpty()) return

    HorizontalDivider(
        color = colors.textTertiary,
        thickness = 1.dp,
        modifier = Modifier.padding(bottom = 32.dp, start = 24.dp, end = 24.dp).background(Color.Transparent),
    )

    Text(
        text = "Riwayat Terbaru",
        fontFamily = appFont,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = colors.textPrimary,
        modifier = Modifier
            .align(Alignment.Start)
            .padding(bottom = 2.dp, start = 24.dp, end = 24.dp)
            .drawBehind {
                val strokeWidthPx = 1.dp.toPx()
                val gapPx = 3.dp.toPx()
                val yOffset = size.height + gapPx
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, yOffset),
                    end = Offset(size.width, yOffset),
                    strokeWidth = strokeWidthPx
                )
            }
    )

    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LazyRow(
        state = listState,
        flingBehavior = snapFlingBehavior,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
    ) {
        items(items) { item ->
            HistoryCard(
                item = item,
                colors = colors,
                appFont = appFont,
                modifier = Modifier.fillParentMaxWidth()
            )
        }
    }
}