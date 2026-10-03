package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun ResultCard(
    cnnResult: List<String>,
    chunkResult: List<String>,
    lstmResult: String,
    precision: String,
    datetime: String,
    colors: AksoroColors,
    appFont: FontFamily,
    style: ResultStyle = ResultStyle.Home,
) {
    val outlineColor = Color(0xFF8A7266)
    var showDeteksi by remember { mutableStateOf(false) }
    var showCaraMembaca by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.btnSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, colors.btnPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (precision.isNotEmpty() || datetime.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (precision.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFC6453A), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Presisi $precision",
                                fontFamily = appFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                    if (datetime.isNotEmpty()) {
                        Text(
                            text = datetime,
                            fontFamily = appFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFFC6453A)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(style.sectionGap))
            }

            // Section: Hasil Transliterasi
            SectionLabel("HASIL TRANSLITERASI", colors, appFont, style)
            Spacer(modifier = Modifier.height(style.resultLabelGap))
            TransliterationResultBox(lstmResult, colors, appFont, style)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = outlineColor.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Section: Hasil Deteksi (Expandable)
            ExpandableSectionHeader(
                title = "HASIL DETEKSI",
                isExpanded = showDeteksi,
                onToggle = { showDeteksi = !showDeteksi },
                colors = colors,
                appFont = appFont,
                style = style
            )
            if (showDeteksi) {
                DetectionChips(cnnResult, colors, appFont, style)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Section: Cara Membaca (Expandable)
            ExpandableSectionHeader(
                title = "CARA MEMBACA",
                isExpanded = showCaraMembaca,
                onToggle = { showCaraMembaca = !showCaraMembaca },
                colors = colors,
                appFont = appFont,
                style = style
            )
            if (showCaraMembaca) {
                ReadingChunkList(chunkResult, colors, appFont, style)
            }
        }
    }
}