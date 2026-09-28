package com.dika.myaksoro.ui.screens

// --- Java & Android Framework ---
import android.content.Context
import java.io.File

// --- Jetpack Compose: Layout & Foundation ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & State Management ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// --- Jetpack Compose: UI, Modifier, Font & Units ---
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- Proyek Internal: Data Layer ---
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.data.HistoryManager

// --- Proyek Internal: Custom Components ---
import com.dika.myaksoro.ui.components.DeleteHistoryDialog
import com.dika.myaksoro.ui.components.HistoryCard

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun HistoryScreen(
    context: Context,
    historyList: MutableList<HistoryItem>,
    colors: AksoroColors,
    appFont: FontFamily
) {
    var isAscending by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val displayedList = if (isAscending) historyList.reversed() else historyList.toList()

    if (showDeleteDialog) {
        DeleteHistoryDialog(
            colors = colors,
            appFont = appFont,
            onConfirm = {
                historyList.forEach {
                    val file = File(it.imagePath)
                    if (file.exists()) file.delete()
                }
                historyList.clear()
                HistoryManager.saveHistory(context, historyList)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgApp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            if (historyList.isNotEmpty()) {
                HistoryToolbar(
                    isAscending = isAscending,
                    colors = colors,
                    appFont = appFont,
                    onToggleSort = { isAscending = !isAscending },
                    onDeleteAll = { showDeleteDialog = true }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada riwayat transliterasi.",
                            fontFamily = appFont,
                            fontWeight = FontWeight.Medium,
                            color = colors.textTertiary
                        )
                    }
                } else {
                    displayedList.forEach { item ->
                        HistoryCard(item = item, colors = colors, appFont = appFont)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}