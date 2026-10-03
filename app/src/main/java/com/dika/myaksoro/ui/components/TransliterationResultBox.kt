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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

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
            .background(Color(0xFF302018), RoundedCornerShape(style.resultRadius))
            .padding(vertical = style.resultPadV, horizontal = style.resultPadH),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = appFont,
            fontSize = style.resultFont,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}