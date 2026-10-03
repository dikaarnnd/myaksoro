package com.dika.myaksoro.ui.screens

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material Icons ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList

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
    onDeleteAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onToggleSort,
            colors = ButtonDefaults.buttonColors(containerColor = colors.btnSecondary),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "Filter Urutan",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.padding(end = 4.dp).size(18.dp)
            )
            Text(
                text = "Urutkan: ",
                fontFamily = appFont,
                fontWeight = FontWeight.Bold,
                color = colors.textOnSecondary,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = if (isAscending) "Terdahulu" else "Terbaru",
                fontFamily = appFont,
                fontWeight = FontWeight.Normal,
                color = colors.textOnSecondary,
                fontSize = 12.sp,
                letterSpacing = 1.sp
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