package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    style: ResultStyle,
) {
    var showDeteksi by remember { mutableStateOf(false) }
    var showCaraMembaca by remember { mutableStateOf(false) }

    SectionLabel("HASIL TRANSLITERASI", colors, appFont, style)
    Spacer(modifier = Modifier.height(style.resultLabelGap))
    TransliterationResultBox(lstmOutput, colors, appFont, style)

    Spacer(modifier = Modifier.height(style.sectionGap))

    ExpandableSectionHeader(
        title = "HASIL DETEKSI",
        isExpanded = showDeteksi,
        onToggle = { showDeteksi = !showDeteksi },
        colors = colors,
        appFont = appFont,
        style = style,
    )
    if (showDeteksi) {
        DetectionChips(cnnOutput, colors, appFont, style)
    }

    Spacer(modifier = Modifier.height(style.sectionGap))

    ExpandableSectionHeader(
        title = "CARA MEMBACA",
        isExpanded = showCaraMembaca,
        onToggle = { showCaraMembaca = !showCaraMembaca },
        colors = colors,
        appFont = appFont,
        style = style,
    )
    if (showCaraMembaca) {
        ReadingChunkList(chunkResults, colors, appFont, style)
    }
}