package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun SectionLabel(
    text: String,
    colors: AksoroColors,
    appFont: FontFamily,
    style: ResultStyle
) {
    Text(
        text = text,
        fontFamily = appFont,
        fontSize = style.labelSize,
        fontWeight = FontWeight.ExtraBold,
        color = colors.textSecondary,
        letterSpacing = style.labelSpacing
    )
}