package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// --- Proyek Internal: Data & Theme ---
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun HistoryCard(
    item: HistoryItem,
    colors: AksoroColors,
    appFont: FontFamily
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.boxHistorySecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            item.bitmapCache?.let { bitmap ->
                ZoomableImage(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Gambar Riwayat",
                    appFont = appFont,
                    isHistoryCard = true
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                TransliterationResultContent(
                    cnnOutput = item.cnnOutput,
                    chunkResults = item.chunkResults,
                    lstmOutput = item.lstmOutput,
                    colors = colors,
                    appFont = appFont,
                    style = ResultStyle.Compact
                )
            }
        }
    }
}