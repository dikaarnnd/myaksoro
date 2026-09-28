package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun TransliterationResultBox(
    text: String,
    colors: AksoroColors,
    appFont: FontFamily,
    style: ResultStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.btnPrimary, RoundedCornerShape(style.resultRadius))
            .padding(vertical = style.resultPadV, horizontal = style.resultPadH),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = appFont,
            fontSize = style.resultFont,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = colors.textOnPrimary,
            textAlign = TextAlign.Center
        )
    }
}