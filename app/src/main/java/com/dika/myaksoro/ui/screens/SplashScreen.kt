package com.dika.myaksoro.ui.screens

// --- Jetpack Compose: Foundation & Animation ---
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

// --- Jetpack Compose: Material Icons & Material 3 ---
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color

// --- Kotlin Coroutines ---
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun SplashScreen(
    colors: AksoroColors,
    appFont: FontFamily,
    onSplashFinished: () -> Unit,
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgApp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Judul Aplikasi
            Text(
                text = "AKSORO",
                fontFamily = appFont,
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle / Tagline
            Text(
                text = "Javanese Handwriting Transliterator",
                fontFamily = appFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                letterSpacing = 1.5.sp
            )
        }

        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alpha.value)
        ) {
//            CircularProgressIndicator(
//                modifier = Modifier.size(24.dp),
//                color = colors.btnAccent,
//                strokeWidth = 2.5.dp
//            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v1.0.0 • PyTorch & OpenCV Engine",
                fontFamily = appFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textTertiary,
                letterSpacing = 1.sp
            )
        }
    }
}

@Preview(showBackground = true, device = "id:medium_phone")
@Composable
fun SplashScreenPreview() {
    SplashScreen(
        colors = AksoroColors(
            bgApp = Color(0xFFFAF7F3),
            bgTopBar = Color(0xFFFFFFFF),
            bgNavBar = Color(0xFFFFFFFF),
            btnPrimary = Color(0xFF2B1B12),
            btnSecondary = Color(0xFFF4EDE4),
            btnAccent = Color(0xFFC0392B),
            textPrimary = Color(0xFF211510),
            textSecondary = Color(0xFF6B5C4F),
            textTertiary = Color(0xFFA79A8C),
            textOnPrimary = Color(0xFFFFFFFF),
            textOnSecondary = Color(0xFF211510),
            boxHistoryPrimary = Color(0xFFF4EDE4),
            boxHistorySecondary = Color(0xFFFFFFFF),
            cardBg = Color(0xFFFFFFFF),
            overlayBg = Color(0x8C140E0A)
        ),
        appFont = FontFamily.Default,
        onSplashFinished = {}
    )
}