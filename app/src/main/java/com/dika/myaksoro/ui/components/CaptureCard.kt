package com.dika.myaksoro.ui.components

// --- Android Framework & Graphics ---
import android.graphics.Bitmap

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun CaptureCard(
    bitmap: Bitmap?,
    colors: AksoroColors,
    appFont: FontFamily
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .wrapContentHeight()
            .heightIn(max = 450.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.btnAccent),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg)
    ) {
        if (bitmap != null) {
            ZoomableImage(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Preview Gambar",
                appFont = appFont,
                isHistoryCard = false
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada gambar",
                    fontFamily = appFont,
                    fontWeight = FontWeight.Medium,
                    color = colors.textTertiary
                )
            }
        }
    }
}