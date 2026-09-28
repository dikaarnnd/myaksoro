package com.dika.myaksoro.ui.screens

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material Icons ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun HistoryToolbar(
    isAscending: Boolean,
    colors: AksoroColors,
    appFont: FontFamily,
    onToggleSort: () -> Unit,
    onDeleteAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onToggleSort,
            colors = ButtonDefaults.buttonColors(containerColor = colors.btnSecondary),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = if (isAscending) "Waktu: Terdahulu" else "Waktu: Terbaru",
                fontFamily = appFont,
                fontWeight = FontWeight.Bold,
                color = colors.textOnSecondary,
                fontSize = 12.sp
            )
        }

        IconButton(
            onClick = onDeleteAll,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Hapus Semua",
                tint = Color(0xFFD32F2F)
            )
        }
    }
}