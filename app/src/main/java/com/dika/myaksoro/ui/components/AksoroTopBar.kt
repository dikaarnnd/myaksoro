package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Material Icons ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AksoroTopBar(
    title: String,
    isDarkTheme: Boolean,
    colors: AksoroColors,
    appFont: FontFamily,
    onToggleTheme: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.shadow(elevation = 6.dp),
        title = {
            Text(
                text = title,
                fontFamily = appFont,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary,
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            )
        },
        actions = {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    contentDescription = "Toggle Theme",
                    tint = colors.textPrimary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colors.bgTopBar
        )
    )
}