package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun ReadingChunkList(
    chunks: List<String>,
    colors: AksoroColors,
    appFont: FontFamily,
    style: ResultStyle
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = style.listTopPad),
        verticalArrangement = Arrangement.spacedBy(style.chunkGap)
    ) {
        chunks.forEachIndexed { idx, chunk ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgApp, RoundedCornerShape(style.chunkRadius))
                    .padding(style.chunkPad)
            ) {
                Text(
                    text = "${idx + 1}.  $chunk",
                    fontFamily = appFont,
                    fontSize = style.chunkFont,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    lineHeight = style.chunkLineHeight,
                    color = colors.textPrimary
                )
            }
        }
    }
}