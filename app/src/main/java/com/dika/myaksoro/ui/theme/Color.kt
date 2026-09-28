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
)

val darkModeColors = AksoroColors(
    bgApp = Color(0xFF16110D),
    bgTopBar = Color(0xFF241B14),
    bgNavBar = Color(0xFF241B14),
    btnPrimary = Color(0xFFF4EDE4),
    btnSecondary = Color(0xFF3A2C22),
    btnAccent = Color(0xFFE05C48),
    textPrimary = Color(0xFFF4EDE4),
    textSecondary = Color(0xFFBFB0A0),
    textTertiary = Color(0xFF8A7B6D),
    textOnPrimary = Color(0xFF16110D),
    textOnSecondary = Color(0xFFF4EDE4),
    boxHistoryPrimary = Color(0xFF241B14),
    boxHistorySecondary = Color(0xFF2E2219),
    cardBg = Color(0xFF241B14),
    overlayBg = Color(0xD9000000)
)