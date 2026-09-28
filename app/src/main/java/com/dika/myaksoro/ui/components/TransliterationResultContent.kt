package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun TransliterationResultContent(
    cnnOutput: List<String>,
    chunkResults: List<String>,
    lstmOutput: String,
    colors: AksoroColors,
    appFont: FontFamily,
    style: ResultStyle
) {
    SectionLabel("HASIL DETEKSI", colors, appFont, style)
    DetectionChips(cnnOutput, colors, appFont, style)
    Spacer(modifier = Modifier.height(style.sectionGap))

    if (chunkResults.isNotEmpty()) {
        SectionLabel("CARA MEMBACA", colors, appFont, style)
        ReadingChunkList(chunkResults, colors, appFont, style)
        Spacer(modifier = Modifier.height(style.sectionGap))
    }

    SectionLabel("HASIL TRANSLITERASI", colors, appFont, style)
    Spacer(modifier = Modifier.height(style.resultLabelGap))
    TransliterationResultBox(lstmOutput, colors, appFont, style)
}