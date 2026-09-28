package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun DetectionChips(
    items: List<String>,
    colors: AksoroColors,
    appFont: FontFamily,
    style: ResultStyle
) {
    if (items.isEmpty()) {
        Text(
            text = "-",
            fontFamily = appFont,
            fontSize = style.chipFont,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
        return
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = style.listTopPad),
        horizontalArrangement = Arrangement.spacedBy(style.chipGap)
    ) {
        items(items.size) { idx ->
            Box(
                modifier = Modifier
                    .background(colors.bgApp, RoundedCornerShape(style.chipRadius))
                    .padding(horizontal = style.chipPadH, vertical = style.chipPadV)
            ) {
                Text(
                    text = "${idx + 1}. ${items[idx]}",
                    fontFamily = appFont,
                    fontSize = style.chipFont,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = colors.textPrimary
                )
            }
        }
    }
}