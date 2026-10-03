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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Data & Theme ---
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun HistoryCard(
    item: HistoryItem,
    colors: AksoroColors,
    appFont: FontFamily,
    modifier: Modifier = Modifier,
    style: ResultStyle = ResultStyle.Compact,
) {
    val outlineColor = Color(0xFF8A7266)
    var showDeteksi by remember { mutableStateOf(false) }
    var showCaraMembaca by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.btnSecondary),
        border = BorderStroke(0.5.dp, colors.btnPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            // Box Tampilan Hasil Segmentasi Gambar
            item.bitmapCache?.let { bitmap ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(max = 180.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg)
                ) {
                    ZoomableImage(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Hasil Segmentasi Gambar",
                        colors = colors,
                        appFont = appFont,
                        isHistoryCard = true
                    )
                }
            }

            Column (
                modifier = Modifier.padding(16.dp)
            ) {
                // Label Presisi & Datetime (layout sama dengan ResultCard)
                if (item.precision.isNotEmpty() || item.datetime.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.precision.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFC6453A), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Presisi ${item.precision}",
                                    fontFamily = appFont,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(1.dp))
                        }
                        if (item.datetime.isNotEmpty()) {
                            Text(
                                text = item.datetime,
                                fontFamily = appFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFFC6453A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section: Hasil Transliterasi
                SectionLabel("HASIL TRANSLITERASI", colors, appFont, style)
                Spacer(modifier = Modifier.height(style.resultLabelGap))
                TransliterationResultBox(item.lstmOutput, colors, appFont, style)

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
                    style = style,
                )
                if (showDeteksi) {
                    DetectionChips(item.cnnOutput, colors, appFont, style)
                }

//                Spacer(modifier = Modifier.height(style.sectionGap))
                Spacer(modifier = Modifier.height(8.dp))

                // Section: Cara Membaca (Expandable)
                ExpandableSectionHeader(
                    title = "CARA MEMBACA",
                    isExpanded = showCaraMembaca,
                    onToggle = { showCaraMembaca = !showCaraMembaca },
                    colors = colors,
                    appFont = appFont,
                    style = style,
                )
                if (showCaraMembaca) {
                    ReadingChunkList(item.chunkResults, colors, appFont, style)
                }
            }


        }
    }
}