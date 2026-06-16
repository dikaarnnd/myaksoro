package com.dika.myaksoro.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

data class AksoroColors(
    val bgApp: Color,
    val bgTopBar: Color,
    val bgNavBar: Color,
    val btnPrimary: Color,
    val btnSecondary: Color,
    val btnAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnPrimary: Color,
    val textOnSecondary: Color,
    val boxHistoryPrimary: Color,
    val boxHistorySecondary: Color,
    val cardBg: Color,
    val overlayBg: Color
)

val lightModeColors = AksoroColors(
    bgApp = Color(0xFFF5F5F5),
    bgTopBar = Color(0xFFFFFFFF),
    bgNavBar = Color(0xFFFFFFFF),
    btnPrimary = Color(0xFF1E1E1E),
    btnSecondary = Color(0xFFE0E0E0),
    btnAccent = Color(0xFF1E1E1E),
    textPrimary = Color(0xFF121212),
    textSecondary = Color(0xFF616161),
    textTertiary = Color(0xFF9E9E9E),
    textOnPrimary = Color(0xFFFFFFFF),
    textOnSecondary = Color(0xFF121212),
    boxHistoryPrimary = Color(0xFFEEEEEE),
    boxHistorySecondary = Color(0xFFFFFFFF),
    cardBg = Color(0xFFFFFFFF),
    overlayBg = Color(0xD9000000)
)

val darkModeColors = AksoroColors(
    bgApp = Color(0xFF121212),
    bgTopBar = Color(0xFF1E1E1E),
    bgNavBar = Color(0xFF1E1E1E),
    btnPrimary = Color(0xFFEEEEEE),
    btnSecondary = Color(0xFF333333),
    btnAccent = Color(0xFFEEEEEE),
    textPrimary = Color(0xFFEEEEEE),
    textSecondary = Color(0xFFAAAAAA),
    textTertiary = Color(0xFF777777),
    textOnPrimary = Color(0xFF121212),
    textOnSecondary = Color(0xFFEEEEEE),
    boxHistoryPrimary = Color(0xFF1E1E1E),
    boxHistorySecondary = Color(0xFF2C2C2C),
    cardBg = Color(0xFF1E1E1E),
    overlayBg = Color(0xD9000000)
)