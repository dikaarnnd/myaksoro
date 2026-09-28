package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun DeleteHistoryDialog(
    colors: AksoroColors,
    appFont: FontFamily,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Hapus Semua Riwayat?",
                fontFamily = appFont,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                "Tindakan ini tidak dapat dibatalkan dan semua gambar akan dihapus permanen dari memori HP-mu.",
                fontFamily = appFont,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Hapus",
                    color = Color(0xFFD32F2F),
                    fontFamily = appFont,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Batal",
                    color = colors.textPrimary,
                    fontFamily = appFont,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = colors.cardBg
    )
}